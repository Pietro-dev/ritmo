package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.TopicoInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.services.DisciplinaService;
import app.ritmo.aplicacaoritmo.services.TopicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/disciplinas/{disciplinaId}/topicos")
public class TopicoController {

    private final TopicoService topicoService;
    private final DisciplinaService disciplinaService;

    @GetMapping
    public String listar(@PathVariable Long disciplinaId, Model model) {
        carregarContexto(disciplinaId, model);
        model.addAttribute("topicoInput", new TopicoInputDTO(""));

        return "topicos/lista";
    }

    @PostMapping
    public String cadastrar(@PathVariable Long disciplinaId, @Valid @ModelAttribute("topicoInput") TopicoInputDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            carregarContexto(disciplinaId, model);
            return "topicos/lista";
        }

        try {
            topicoService.cadastrar(disciplinaId, dto);
            return "redirect:/disciplinas/" + disciplinaId + "/topicos";
        } catch (EntidadeDuplicadaException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            carregarContexto(disciplinaId, model);
            return "topicos/lista";
        }
    }

    @GetMapping("/{topicoId}/editar")
    public String exibirFormularioEdicao(@PathVariable Long disciplinaId, @PathVariable Long topicoId, Model model) {
        carregarContexto(disciplinaId, model);
        model.addAttribute("topicoId", topicoId);
        model.addAttribute("topicoInput", new TopicoInputDTO(topicoService.buscarPeloId(disciplinaId, topicoId).nome()));

        return "topicos/editar";
    }

    @PutMapping("/{topicoId}")
    public String atualizar(@PathVariable Long disciplinaId, @PathVariable Long topicoId, @Valid @ModelAttribute("topicoInput") TopicoInputDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            carregarContexto(disciplinaId, model);
            model.addAttribute("topicoId", topicoId);
            return "topicos/editar";
        }

        try {
            topicoService.atualizar(disciplinaId, topicoId, dto);
            return "redirect:/disciplinas/" + disciplinaId + "/topicos";
        } catch (EntidadeDuplicadaException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            carregarContexto(disciplinaId, model);
            model.addAttribute("topicoId", topicoId);

            return "topicos/editar";
        }
    }

    @DeleteMapping("/{topicoId}")
    public String excluir(@PathVariable Long disciplinaId, @PathVariable Long topicoId) {
        topicoService.excluir(disciplinaId, topicoId);
        return "redirect:/disciplinas/" + disciplinaId + "/topicos";
    }

    // Metodo auxiliar que envia os dados necessários para renderizar a página
    private void carregarContexto(Long disciplinaId, Model model) {
        model.addAttribute("paginaAtiva", "disciplinas");
        model.addAttribute("disciplina", disciplinaService.buscarPeloIdDoUsuario(disciplinaId));
        model.addAttribute("topicos", topicoService.listarTopicosDaDisciplina(disciplinaId));
    }
}