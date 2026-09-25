package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.services.FraseMotivacionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final FraseMotivacionalService fraseMotivacionalService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute(
                "frase",
                fraseMotivacionalService.buscarFraseETraduzir().orElse(null)
        );
        return "dashboard";
    }
}
