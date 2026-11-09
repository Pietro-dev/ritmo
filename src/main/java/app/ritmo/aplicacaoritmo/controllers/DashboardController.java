package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.dto.PeriodoDashboard;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.services.DashboardService;
import app.ritmo.aplicacaoritmo.services.FraseMotivacionalService;
import app.ritmo.aplicacaoritmo.services.PainelAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final FraseMotivacionalService fraseMotivacionalService;
    private final SecurityUtils securityUtils;
    private final DashboardService dashboardService;
    private final PainelAdminService painelAdminService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "TRINTA_DIAS") PeriodoDashboard periodo,
            Model model
    ) {
        var usuario = securityUtils.getUsuarioLogado();
        model.addAttribute("paginaAtiva", "dashboard");
        model.addAttribute("usuario", usuario);
        model.addAttribute("ehAdmin", usuario.getRole() == Role.ROLE_ADMIN);
        model.addAttribute("frase", fraseMotivacionalService.buscarFraseETraduzir().orElse(null));
        model.addAttribute("periodos", Arrays.asList(PeriodoDashboard.values()));
        model.addAttribute("periodoSelecionado", periodo);
        model.addAttribute("resumo", dashboardService.carregarResumo(periodo));
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("painelAdmin", painelAdminService.carregar(periodo));
        }
        return "dashboard";
    }
}
