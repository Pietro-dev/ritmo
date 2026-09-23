package app.ritmo.aplicacaoritmo.controllers;


import app.ritmo.aplicacaoritmo.dto.DisciplinaInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.services.DisciplinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/disciplinas")
public class DisciplinaController {

    private final DisciplinaService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("disciplinas", service.listarDoUsuarioLogado());
        model.addAttribute("disciplinaInput", new DisciplinaInputDTO(""));

        return "disciplinas/lista";
    }

    @GetMapping("/nova")
    public String exibirFormularioCriacao(Model model) {
        model.addAttribute("disciplinaInput",new DisciplinaInputDTO(""));

        return "disciplinas/nova";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("disciplinaInput") DisciplinaInputDTO dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("disciplinas", service.listarDoUsuarioLogado());
            return "disciplinas/lista";
        }

        try {
            service.cadastrar(dto);
            return "redirect:/disciplinas";
        } catch (EntidadeDuplicadaException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            model.addAttribute("disciplinas", service.listarDoUsuarioLogado());
            return "disciplinas/lista";
        }
    }

    @GetMapping("/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("disciplinaId", id);
        model.addAttribute("disciplinaInput",service.buscarPeloIdDoUsuario(id));

        return "disciplinas/editar";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("disciplinaInput") DisciplinaInputDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("disciplinaId", id);
            return "disciplinas/editar";
        }

        try {
            service.atualizar(id, dto);
            return "redirect:/disciplinas";

        } catch (NegocioException ex) {
            result.rejectValue("nome","duplicado",ex.getMessage());

            model.addAttribute("disciplinaId", id);

            return "disciplinas/editar";
        }
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id) {
        service.excluir(id);
        return "redirect:/disciplinas";
    }

}