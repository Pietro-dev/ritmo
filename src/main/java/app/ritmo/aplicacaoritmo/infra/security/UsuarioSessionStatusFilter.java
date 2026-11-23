package app.ritmo.aplicacaoritmo.infra.security;

import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UsuarioSessionStatusFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);

            if (usuario == null) {
                encerrarSessao(request);
                SecurityContextHolder.clearContext();
                excluirCookieSessao(response);
                response.sendRedirect("/login?contaExcluida");
                return;
            }

            if (usuario.getStatus() == StatusCadastro.INATIVO) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    if (usuario.getMotivoInativacao() != null) {
                        session.setAttribute("motivoInativacao", usuario.getMotivoInativacao());
                    }
                    if (usuario.getInativadoAte() != null) {
                        session.setAttribute("inativadoAte", usuario.getInativadoAte());
                    }
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
                excluirCookieSessao(response);

                HttpSession loginSession = request.getSession(true);
                if (usuario.getMotivoInativacao() != null) {
                    loginSession.setAttribute("motivoInativacao", usuario.getMotivoInativacao());
                }
                if (usuario.getInativadoAte() != null) {
                    loginSession.setAttribute("inativadoAte", usuario.getInativadoAte());
                }
                response.sendRedirect("/login?inativo");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void encerrarSessao(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void excluirCookieSessao(HttpServletResponse response) {
        response.setHeader(
                "Set-Cookie",
                "JSESSIONID=; Path=/; Max-Age=0; HttpOnly"
        );
    }
}
