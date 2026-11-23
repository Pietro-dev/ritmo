package app.ritmo.aplicacaoritmo.infra.security;

import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import app.ritmo.aplicacaoritmo.infra.security.EmailHash;
import java.time.Clock;
import java.time.LocalDateTime;
import app.ritmo.aplicacaoritmo.infra.config.ClockConfig;

@Component
@RequiredArgsConstructor
public class LoginFalhouHandler implements AuthenticationFailureHandler {
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String email = request.getParameter("email");
        if (email != null) {
            usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                if (usuario.getStatus() == StatusCadastro.INATIVO) {
                    request.getSession().setAttribute(
                            "motivoInativacao",
                            usuario.getMotivoInativacao()
                    );
                    request.getSession().setAttribute(
                            "inativadoAte",
                            usuario.getInativadoAte()
                    );
                }
            });
        }

        usuarioRepository.findByEmail(email)
                .filter(usuario -> usuario.getStatus() == StatusCadastro.INATIVO)
                .ifPresentOrElse(
                        usuario -> {
                            try {
                                response.sendRedirect("/login?inativo");
                            } catch (IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        },
                        () -> {
                            try {
                                response.sendRedirect("/login?erro");
                            } catch (IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                );
    }
}
