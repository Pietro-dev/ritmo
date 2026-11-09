package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.PerguntaInputDTO;
import app.ritmo.aplicacaoritmo.services.DisciplinaService;
import app.ritmo.aplicacaoritmo.services.PerguntaService;
import app.ritmo.aplicacaoritmo.services.TopicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/disciplinas/{disciplinaId}/topicos/{topicoId}/perguntas")
public class PerguntaController {

    private final PerguntaService perguntaService;
    private final TopicoService topicoService;
    private final DisciplinaService disciplinaService;

    @GetMapping
    public String listar(@PathVariable Long disciplinaId, @PathVariable Long topicoId, Model model) {
        carregarContexto(disciplinaId, topicoId, model);
        model.addAttribute("perguntaInput", new PerguntaInputDTO("", ""));

        return "perguntas/lista";
    }

    @PostMapping
    public String cadastrar(@PathVariable Long disciplinaId, @PathVariable Long topicoId, @Valid @ModelAttribute("perguntaInput") PerguntaInputDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            carregarContexto(disciplinaId, topicoId, model);

            return "perguntas/lista";
        }

        perguntaService.cadastrar(disciplinaId, topicoId, dto);

        return urlDaLista(disciplinaId, topicoId);
    }

    @GetMapping("/{perguntaId}/editar")
    public String exibirFormularioEdicao(@PathVariable Long disciplinaId, @PathVariable Long topicoId, @PathVariable Long perguntaId, Model model) {
        carregarContexto(disciplinaId, topicoId, model);
        model.addAttribute("perguntaId", perguntaId);
        model.addAttribute("perguntaInput", perguntaService.buscarParaEdicao(disciplinaId, topicoId, perguntaId));

        return "perguntas/editar";
    }

    @PutMapping("/{perguntaId}")
    public String atualizar(@PathVariable Long disciplinaId, @PathVariable Long topicoId, @PathVariable Long perguntaId, @Valid @ModelAttribute("perguntaInput") PerguntaInputDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            carregarContexto(disciplinaId, topicoId, model);
            model.addAttribute("perguntaId", perguntaId);

            return "perguntas/editar";
        }

        perguntaService.atualizar(disciplinaId, topicoId, perguntaId, dto);

        return urlDaLista(disciplinaId, topicoId);
    }

    @DeleteMapping("/{perguntaId}")
    public String excluir(@PathVariable Long disciplinaId, @PathVariable Long topicoId, @PathVariable Long perguntaId) {
        perguntaService.excluir(disciplinaId, topicoId, perguntaId);

        return urlDaLista(disciplinaId, topicoId);
    }

    /*
    * MÉTODOS AUXILIARES
    * */

    // Metodo para renderizar tela
    private void carregarContexto(Long disciplinaId, Long topicoId, Model model) {
        model.addAttribute("paginaAtiva", "disciplinas");
        model.addAttribute("disciplina", disciplinaService.buscarPeloIdDoUsuario(disciplinaId));
        model.addAttribute("topico", topicoService.buscarPeloId(disciplinaId, topicoId));
        model.addAttribute("perguntas", perguntaService.listarPerguntasDoTopico(disciplinaId, topicoId));
    }

    // metodo para evitar duplicidade na chamada de POSTs ou DELETEs no recarregamento da página.
    private String urlDaLista(Long disciplinaId, Long topicoId) {
        return "redirect:/disciplinas/" + disciplinaId
                + "/topicos/" + topicoId + "/perguntas";
    }
}
