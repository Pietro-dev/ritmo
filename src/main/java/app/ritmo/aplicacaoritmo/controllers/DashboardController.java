package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.services.FraseMotivacionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final FraseMotivacionalService fraseMotivacionalService;
    private final SecurityUtils securityUtils;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var usuario = securityUtils.getUsuarioLogado();

        model.addAttribute("usuario", usuario);
        model.addAttribute("ehAdmin", usuario.getRole() == Role.ROLE_ADMIN);
        model.addAttribute("frase", fraseMotivacionalService.buscarFraseETraduzir().orElse(null));

        return "dashboard";
    }
}
