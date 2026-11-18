package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioAuthServiceTest {

    @Test
    void usuarioAtivoPodeSeAutenticar() {
        UserDetails detalhes = carregarUsuario(StatusCadastro.ATIVO);

        assertTrue(detalhes.isEnabled());
    }

    @Test
    void usuarioInativoNaoPodeSeAutenticar() {
        UserDetails detalhes = carregarUsuario(StatusCadastro.INATIVO);

        assertFalse(detalhes.isEnabled());
    }

    private UserDetails carregarUsuario(StatusCadastro status) {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "hash",
                Role.ROLE_ESTUDANTE,
                status,
                LocalDateTime.now()
        );
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        UsuarioAuthService service = new UsuarioAuthService(repository);

        return service.loadUserByUsername(usuario.getEmail());
    }
}
