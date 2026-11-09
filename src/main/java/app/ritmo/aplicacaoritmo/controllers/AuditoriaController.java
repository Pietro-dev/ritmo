package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication authentication
    ) {
        model.addAttribute(
                "auditorias",
                auditoriaService.pesquisar(email, inicio, fim, PageRequest.of(Math.max(page, 0), 25))
        );

        model.addAttribute("email", email);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("paginaAtiva", "auditorias");

        auditoriaService.registrar(authentication.getName(), AcaoAuditoria.CONSULTAR_AUDITORIAS, "Administrador consultou os registros de auditoria");

        return "/admin/auditorias";
    }
}
