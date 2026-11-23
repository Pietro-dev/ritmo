package app.ritmo.aplicacaoritmo.infra.jobs;

import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InativacaoUsuarioJob {
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void reativarInativacoesExpiradas() {
        log.info("Job de reativação automática iniciado.");
        try {
            LocalDateTime agora = LocalDateTime.now(clock);
            int reativados = 0;
            var usuariosExpirados = usuarioRepository.findAll().stream()
                    .filter(usuario -> usuario.getStatus() == StatusCadastro.INATIVO)
                    .filter(usuario -> usuario.getInativadoAte() != null)
                    .filter(usuario -> !agora.isBefore(usuario.getInativadoAte()))
                    .toList();

            for (var usuario : usuariosExpirados) {
                usuario.setStatus(StatusCadastro.ATIVO);
                usuario.setMotivoInativacao(null);
                usuario.setInativadoEm(null);
                usuario.setInativadoAte(null);
            }

            reativados = usuariosExpirados.size();
            usuarioRepository.flush();
            log.info("Job de reativação automática concluído: {} usuário(s) reativado(s).", reativados);
        } catch (Exception ex) {
            log.error("Job de reativação automática falhou.", ex);
            throw ex;
        }
    }
}
