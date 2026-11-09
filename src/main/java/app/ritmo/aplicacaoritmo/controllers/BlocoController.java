package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.services.DisciplinaService;
import app.ritmo.aplicacaoritmo.services.SessaoService;
import app.ritmo.aplicacaoritmo.services.TopicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/blocos/configuracao")
public class BlocoController {

    private final DisciplinaService disciplinaService;
    private final TopicoService topicoService;
    private final SessaoService sessaoService;

    // Exibe o formulário e, se houver disciplina selecionada, carrega seus tópicos.
    @GetMapping
    public String exibirFormulario(@RequestParam(required = false) Long disciplinaId, Model model) {
        BlocoInputDTO formulario = new BlocoInputDTO(disciplinaId, 5, List.of());

        carregarFormulario(disciplinaId, formulario, model);
        return "blocos/configuracao";
    }

    // Valida a configuração, cria a sessão pendente e abre seu resumo.
    @PostMapping
    public String configurar(
            @Valid @ModelAttribute("configuracaoInput") BlocoInputDTO formulario,
            BindingResult resultado,
            Model model
    ) {
        if (resultado.hasErrors()) {
            carregarFormulario(formulario.disciplinaId(), formulario, model);
            return "blocos/configuracao";
        }

        try {
            Long sessaoId = sessaoService.criar(formulario);
            return "redirect:/sessoes/" + sessaoId;
        } catch (NegocioException ex) {
            carregarFormulario(formulario.disciplinaId(), formulario, model);
            model.addAttribute("erroConfiguracao", ex.getMessage());
            return "blocos/configuracao";
        }
    }
    private void carregarFormulario(Long disciplinaId, BlocoInputDTO formulario, Model model) {
        var disciplinas = disciplinaService.listarDoUsuarioLogado();

        boolean disciplinaDoUsuario = disciplinaId != null && disciplinas.stream()
                .anyMatch(disciplina -> disciplina.id().equals(disciplinaId));

        model.addAttribute("paginaAtiva", "blocos");
        model.addAttribute("disciplinas", disciplinas);
        model.addAttribute("configuracaoInput", formulario);
        model.addAttribute("disciplinaSelecionadaId", disciplinaDoUsuario ? disciplinaId : null);
        model.addAttribute("topicosDisponiveis", disciplinaDoUsuario ? topicoService.listarTopicosDaDisciplina(disciplinaId) : List.of());
    }
}