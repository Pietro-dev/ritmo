package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.ExclusaoContaInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerfilInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final SecurityUtils securityUtils;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional
    public void atualizarPerfil(PerfilInputDTO dto) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        String nome = dto.nome().trim();
        boolean alterarSenha = dto.novaSenha() != null && !dto.novaSenha().isBlank();

        if (alterarSenha) {
            if (dto.senhaAtual() == null || dto.senhaAtual().isBlank()
                    || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
                throw new NegocioException("A senha atual está incorreta.");
            }
            if (dto.novaSenha().length() < 6) {
                throw new NegocioException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            if (!dto.novaSenha().equals(dto.confirmarNovaSenha())) {
                throw new NegocioException("A confirmação da nova senha não corresponde.");
            }
        } else if (dto.confirmarNovaSenha() != null && !dto.confirmarNovaSenha().isBlank()) {
            throw new NegocioException("Informe a nova senha para confirmar a alteração.");
        }

        boolean nomeAlterado = !usuario.getNome().equals(nome);
        if (!nomeAlterado && !alterarSenha) {
            throw new NegocioException("Nenhuma alteração foi informada.");
        }

        usuario.setNome(nome);
        if (alterarSenha) {
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }
        usuarioRepository.save(usuario);

        String descricao = alterarSenha
                ? (nomeAlterado ? "Atualizou nome e senha do perfil." : "Atualizou a senha do perfil.")
                : "Atualizou o nome do perfil.";
        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.ATUALIZAR_PERFIL, descricao);
    }

    @Transactional
    public void excluirConta(ExclusaoContaInputDTO dto) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        if (dto == null || !dto.confirmarExclusao()) {
            throw new NegocioException("Confirme que deseja excluir sua conta.");
        }
        if (dto.senhaAtual() == null
                || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new NegocioException("A senha atual está incorreta.");
        }
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            long administradoresAtivos = usuarioRepository
                    .findByRoleAndStatus(Role.ROLE_ADMIN, StatusCadastro.ATIVO)
                    .size();
            if (administradoresAtivos <= 1) {
                throw new NegocioException(
                        "Não é possível excluir o único administrador ativo. Cadastre outro administrador antes."
                );
            }
        }

        String email = usuario.getEmail();
        auditoriaRepository.deleteByUsuarioEmailIgnoreCase(email);
        usuarioRepository.delete(usuario);
    }
}
