package app.ritmo.aplicacaoritmo.infra.security;

import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
* Helper de segurança para capturar usuário logado.
* com esse helper evitamos duplicidade dentro dos endpoints
 */
@Component
public class SecurityUtils {
    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario getUsuarioLogado(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Nenhum usuário autenticado encontrado na sessão.");
        }

        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("O usuário com email: " +email+ ". Não foi encontrado!"));
    }
}
