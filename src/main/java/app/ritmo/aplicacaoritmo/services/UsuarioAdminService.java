package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.InativacaoCadastroInputDTO;
import app.ritmo.aplicacaoritmo.dto.UsuarioAdminDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioAdminService {
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final SecurityUtils securityUtils;
    private final Clock clock;

    public List<UsuarioAdminDTO> listar() {
        return usuarioRepository.findAllByOrderByDataCadastroDesc().stream()
                .map(usuario -> new UsuarioAdminDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRole(),
                        usuario.getStatus(),
                        usuario.getMotivoInativacao(),
                        usuario.getInativadoEm(),
                        usuario.getInativadoAte()
                ))
                .toList();
    }

    @Transactional
    public void inativar(Long usuarioId, InativacaoCadastroInputDTO dto) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getStatus() == StatusCadastro.INATIVO) {
            throw new NegocioException("Este cadastro já está inativo.");
        }
        if (dto == null || dto.motivo() == null || dto.motivo().isBlank()) {
            throw new NegocioException("Informe o motivo da inativação.");
        }
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            long adminsAtivos = usuarioRepository
                    .findByRoleAndStatus(Role.ROLE_ADMIN, StatusCadastro.ATIVO)
                    .size();
            if (adminsAtivos <= 1) {
                throw new NegocioException("Não é possível inativar o único administrador ativo.");
            }
        }

        usuario.setStatus(StatusCadastro.INATIVO);
        usuario.setMotivoInativacao(dto.motivo().trim());
        usuario.setInativadoEm(LocalDateTime.now(clock));
        usuario.setInativadoAte(LocalDateTime.now(clock).plusDays(3));
        usuarioRepository.save(usuario);
        auditoriaService.registrar(
                securityUtils.getUsuarioLogado().getEmail(),
                AcaoAuditoria.INATIVAR_CADASTRO,
                "Inativou o cadastro " + usuario.getEmail() + ". Motivo: " + dto.motivo().trim()
        );
    }

    @Transactional
    public void reativar(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getStatus() == StatusCadastro.ATIVO) {
            throw new NegocioException("Este cadastro já está ativo.");
        }
        usuario.setStatus(StatusCadastro.ATIVO);
        usuario.setMotivoInativacao(null);
        usuario.setInativadoEm(null);
        usuario.setInativadoAte(null);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(
                securityUtils.getUsuarioLogado().getEmail(),
                AcaoAuditoria.REATIVAR_CADASTRO,
                "Reativou o cadastro " + usuario.getEmail() + "."
        );
    }

    @Transactional
    public void promover(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            throw new NegocioException("Este usuário já é administrador.");
        }
        if (usuario.getStatus() != StatusCadastro.ATIVO) {
            throw new NegocioException("Somente cadastros ativos podem ser promovidos.");
        }

        usuario.setRole(Role.ROLE_ADMIN);
        usuarioRepository.save(usuario);
        auditoriaService.registrar(
                securityUtils.getUsuarioLogado().getEmail(),
                AcaoAuditoria.PROMOVER_ADMINISTRADOR,
                "Promoveu " + usuario.getEmail() + " a administrador."
        );
    }

    @Transactional
    public void excluir(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            long adminsAtivos = usuarioRepository
                    .findByRoleAndStatus(Role.ROLE_ADMIN, StatusCadastro.ATIVO)
                    .size();
            if (adminsAtivos <= 1) {
                throw new NegocioException("Não é possível excluir o único administrador ativo.");
            }
        }

        String email = usuario.getEmail();
        usuarioRepository.delete(usuario);
        auditoriaService.registrar(
                securityUtils.getUsuarioLogado().getEmail(),
                AcaoAuditoria.EXCLUIR_CADASTRO,
                "Excluiu permanentemente o cadastro " + email + "."
        );
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
    }
}
