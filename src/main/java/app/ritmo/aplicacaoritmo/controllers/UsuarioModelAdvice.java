package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class UsuarioModelAdvice {

    private final SecurityUtils securityUtils;

    @ModelAttribute
    public void adicionarDadosDoUsuario(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        var usuario = securityUtils.getUsuarioLogado();
        model.addAttribute("usuario", usuario);
        model.addAttribute("ehAdmin", usuario.getRole() == Role.ROLE_ADMIN);
    }
}