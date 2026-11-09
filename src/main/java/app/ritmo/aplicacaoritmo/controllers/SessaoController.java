package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.dto.AutoavaliacaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaSessaoDTO;
import app.ritmo.aplicacaoritmo.dto.RespostaPerguntaInputDTO;
import app.ritmo.aplicacaoritmo.services.SessaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sessoes")
public class SessaoController {

    private final SessaoService sessaoService;

    // Exibe o resumo da sessão pendente antes do início.
    @GetMapping({"/{sessaoId:\\d+}", "/resumo/{sessaoId:\\d+}"})
    public String exibirResumo(@PathVariable Long sessaoId, Model model) {
        model.addAttribute("sessao", sessaoService.buscarResumo(sessaoId));
        model.addAttribute("paginaAtiva", "blocos");
        return "sessoes/resumo";
    }

    // Altera a sessão pendente para em andamento e encaminha à primeira pergunta.
    @PostMapping("/{sessaoId:\\d+}/iniciar")
    public String iniciar(@PathVariable Long sessaoId) {
        sessaoService.iniciar(sessaoId);
        return "redirect:/sessoes/" + sessaoId + "/pergunta";
    }

    // O gabarito só é incluído depois do registro da resposta.
    @GetMapping("/{sessaoId:\\d+}/pergunta")
    public String exibirPerguntaAtual(@PathVariable Long sessaoId, Model model) {
        PerguntaSessaoDTO pergunta = sessaoService.buscarPerguntaAtual(sessaoId).orElse(null);
        if (pergunta == null) {
            return "redirect:/sessoes/resumo/" + sessaoId;
        }
        model.addAttribute("pergunta", pergunta);
        model.addAttribute("respostaForm", new RespostaPerguntaInputDTO(""));
        model.addAttribute("avaliacaoForm", new AutoavaliacaoInputDTO(null));
        model.addAttribute("paginaAtiva", "blocos");
        return "sessoes/pergunta";
    }

    @PostMapping("/{sessaoId:\\d+}/resposta")
    public String registrarResposta(
            @PathVariable Long sessaoId,
            @Valid @ModelAttribute("respostaForm") RespostaPerguntaInputDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "erroSessao",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
            return "redirect:/sessoes/" + sessaoId + "/pergunta";
        }

        StatusSessao status = sessaoService.registrarResposta(sessaoId, dto);
        return redirecionarAposAcao(sessaoId, status);
    }

    @PostMapping("/{sessaoId:\\d+}/autoavaliacao")
    public String registrarAutoavaliacao(
            @PathVariable Long sessaoId,
            @Valid @ModelAttribute("avaliacaoForm") AutoavaliacaoInputDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "erroSessao",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
            return "redirect:/sessoes/" + sessaoId + "/pergunta";
        }

        StatusSessao status = sessaoService.registrarAutoavaliacao(sessaoId, dto);
        return redirecionarAposAcao(sessaoId, status);
    }

    @PostMapping("/{sessaoId:\\d+}/interromper")
    public String interromper(@PathVariable Long sessaoId) {
        sessaoService.interromper(sessaoId);
        return "redirect:/sessoes/resumo/" + sessaoId;
    }

    private String redirecionarAposAcao(Long sessaoId, StatusSessao status) {
        if (status == StatusSessao.EM_ANDAMENTO) {
            return "redirect:/sessoes/" + sessaoId + "/pergunta";
        }
        return "redirect:/sessoes/resumo/" + sessaoId;
    }
}