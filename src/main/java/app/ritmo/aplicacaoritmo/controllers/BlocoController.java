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
import org.springframework.data.domain.PageRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/blocos/configuracao")
public class BlocoController {

    private final DisciplinaService disciplinaService;
    private final TopicoService topicoService;
    private final SessaoService sessaoService;

    // Exibe o formulário e, se houver disciplina selecionada, carrega seus tópicos.
    public String exibirFormulario(Long disciplinaId, Model model) {
        return exibirFormulario(disciplinaId, 0, model);
    }

    @GetMapping
    public String exibirFormulario(
            @RequestParam(required = false) Long disciplinaId,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        BlocoInputDTO formulario = new BlocoInputDTO(disciplinaId, 5, List.of());

        carregarFormulario(disciplinaId, formulario, page, model);
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
            carregarFormulario(formulario.disciplinaId(), formulario, 0, model);
            model.addAttribute(
                    "erroConfiguracao",
                    resultado.getAllErrors().stream()
                            .findFirst()
                            .map(erro -> erro.getDefaultMessage())
                            .orElse("Revise os dados informados.")
            );
            return "blocos/configuracao";
        }

        try {
            Long sessaoId = sessaoService.criar(formulario);
            return "redirect:/sessoes/" + sessaoId;
        } catch (NegocioException ex) {
            carregarFormulario(formulario.disciplinaId(), formulario, 0, model);
            model.addAttribute("erroConfiguracao", ex.getMessage());
            return "blocos/configuracao";
        }
    }
    private void carregarFormulario(
            Long disciplinaId,
            BlocoInputDTO formulario,
            int page,
            Model model
    ) {
        var disciplinas = disciplinaService.listarDoUsuarioLogado();

        boolean disciplinaDoUsuario = disciplinaId != null && disciplinas.stream()
                .anyMatch(disciplina -> disciplina.id().equals(disciplinaId));

        model.addAttribute("paginaAtiva", "blocos");
        model.addAttribute("disciplinas", disciplinas);
        model.addAttribute("configuracaoInput", formulario);
        model.addAttribute("disciplinaSelecionadaId", disciplinaDoUsuario ? disciplinaId : null);
        model.addAttribute("topicosDisponiveis", disciplinaDoUsuario ? topicoService.listarTopicosDaDisciplina(disciplinaId) : List.of());
        model.addAttribute(
                "blocos",
                sessaoService.listarDoUsuarioLogado(PageRequest.of(Math.max(page, 0), 6))
        );
    }
}