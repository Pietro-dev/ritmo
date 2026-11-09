package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.BlocoRevisaoInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.services.RevisaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/revisoes")
public class RevisaoController {

    private final RevisaoService revisaoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("revisoes", revisaoService.listarRevisoes());
        model.addAttribute("blocoRevisao", new BlocoRevisaoInputDTO(5, java.util.List.of()));
        model.addAttribute("paginaAtiva", "revisoes");
        return "revisoes/lista";
    }

    @PostMapping("/blocos")
    public String criarBloco(
            @Valid @ModelAttribute("blocoRevisao") BlocoRevisaoInputDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("revisoes", revisaoService.listarRevisoes());
            model.addAttribute("paginaAtiva", "revisoes");
            return "revisoes/lista";
        }

        try {
            Long sessaoId = revisaoService.criarBloco(dto);
            return "redirect:/sessoes/" + sessaoId;
        } catch (NegocioException ex) {
            model.addAttribute("erroRevisao", ex.getMessage());
            model.addAttribute("revisoes", revisaoService.listarRevisoes());
            model.addAttribute("paginaAtiva", "revisoes");
            return "revisoes/lista";
        }
    }
}
