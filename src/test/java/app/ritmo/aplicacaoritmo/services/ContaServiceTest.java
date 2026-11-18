package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.ExclusaoContaInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerfilInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContaServiceTest {

    private SecurityUtils securityUtils;
    private UsuarioRepository usuarioRepository;
    private AuditoriaRepository auditoriaRepository;
    private PasswordEncoder passwordEncoder;
    private AuditoriaService auditoriaService;
    private ContaService contaService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        securityUtils = mock(SecurityUtils.class);
        usuarioRepository = mock(UsuarioRepository.class);
        auditoriaRepository = mock(AuditoriaRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditoriaService = mock(AuditoriaService.class);
        usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "hash",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        contaService = new ContaService(
                securityUtils,
                usuarioRepository,
                auditoriaRepository,
                passwordEncoder,
                auditoriaService
        );
    }

    @Test
    void atualizaNomeSemExporSenhaNaAuditoria() {
        contaService.atualizarPerfil(new PerfilInputDTO("  Novo nome  ", "", "", ""));

        assertEquals("Novo nome", usuario.getNome());
        verify(usuarioRepository).save(usuario);
        verify(auditoriaService).registrar(
                "estudante@example.com",
                app.ritmo.aplicacaoritmo.domain.AcaoAuditoria.ATUALIZAR_PERFIL,
                "Atualizou o nome do perfil."
        );
    }

    @Test
    void exigeConfirmacaoDeSenhaAntesDeTrocarSenha() {
        when(passwordEncoder.matches("senha antiga", "hash")).thenReturn(true);

        contaService.atualizarPerfil(
                new PerfilInputDTO("Estudante", "senha antiga", "nova senha", "nova senha")
        );

        verify(passwordEncoder).encode("nova senha");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void rejeitaSenhaAtualIncorretaSemSalvarAlteracoes() {
        when(passwordEncoder.matches("incorreta", "hash")).thenReturn(false);

        assertThrows(
                NegocioException.class,
                () -> contaService.atualizarPerfil(
                        new PerfilInputDTO("Outro nome", "incorreta", "nova senha", "nova senha")
                )
        );

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void excluiRegistrosDeAuditoriaAntesDaContaDoEstudante() {
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        contaService.excluirConta(new ExclusaoContaInputDTO("senha", true));

        var ordem = inOrder(auditoriaRepository, usuarioRepository);
        ordem.verify(auditoriaRepository).deleteByUsuarioEmailIgnoreCase("estudante@example.com");
        ordem.verify(usuarioRepository).delete(usuario);
    }

    @Test
    void impedeExclusaoDoUnicoAdministradorAtivo() {
        usuario.setRole(Role.ROLE_ADMIN);
        usuario.setStatus(StatusCadastro.ATIVO);
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
        when(usuarioRepository.findByRoleAndStatus(Role.ROLE_ADMIN, StatusCadastro.ATIVO))
                .thenReturn(List.of(usuario));

        assertThrows(
                NegocioException.class,
                () -> contaService.excluirConta(new ExclusaoContaInputDTO("senha", true))
        );

        verify(usuarioRepository, never()).delete(usuario);
    }

    @Test
    void permiteAutoexclusaoDoAdministradorQuandoOutroAdministradorEstaAtivo() {
        usuario.setRole(Role.ROLE_ADMIN);
        usuario.setStatus(StatusCadastro.ATIVO);
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
        when(usuarioRepository.findByRoleAndStatus(Role.ROLE_ADMIN, StatusCadastro.ATIVO))
                .thenReturn(List.of(usuario, new Usuario()));

        contaService.excluirConta(new ExclusaoContaInputDTO("senha", true));

        verify(auditoriaRepository).deleteByUsuarioEmailIgnoreCase("estudante@example.com");
        verify(usuarioRepository).delete(usuario);
    }
}
