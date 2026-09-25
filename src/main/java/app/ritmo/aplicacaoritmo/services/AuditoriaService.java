package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Auditoria;
import app.ritmo.aplicacaoritmo.dto.AuditoriaOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository repository;

    @Transactional
    public void registrar(String usuarioEmail, AcaoAuditoria acao, String descricao) {
        if (acao == null) {
            throw new NegocioException(
                    "A ação da auditoria é obrigatória"
            );
        }

        if (descricao == null || descricao.isBlank()) {
            throw new NegocioException(
                    "A descrição da auditoria é obrigatória"
            );
        }

        Auditoria auditoria = new Auditoria(normalizarEmail(usuarioEmail), acao, descricao.trim());

        repository.save(auditoria);
    }

    @Transactional
    public List<AuditoriaOutputDTO> pesquisar(String email, LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDateTime = null;
        LocalDateTime fimDateTime = null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new NegocioException("A data inicial não pode ser posterior à data final");
        }

        if (inicio != null ){
            inicioDateTime = inicio.atStartOfDay();
        }

        if (fim != null) {
            fimDateTime = fim.plusDays(1).atStartOfDay();
        }

        String emailNormalizado = normalizarEmail(email);

        return repository.pesquisar(emailNormalizado, inicioDateTime, fimDateTime)
                .stream()
                .map(AuditoriaOutputDTO::fromEntity)
                .toList();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}
