package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.dto.PerguntaSessaoDTO;
import app.ritmo.aplicacaoritmo.dto.RespostaPerguntaInputDTO;
import app.ritmo.aplicacaoritmo.services.SessaoService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessaoControllerTest {

    @Test
    void exibePerguntaAtualEEncaminhaRespostaAProximaEtapa() {
        SessaoService service = mock(SessaoService.class);
        SessaoController controller = new SessaoController(service);
        PerguntaSessaoDTO pergunta = mock(PerguntaSessaoDTO.class);
        when(service.buscarPerguntaAtual(12L)).thenReturn(Optional.of(pergunta));

        assertEquals("sessoes/pergunta",
                controller.exibirPerguntaAtual(12L, new ExtendedModelMap()));

        RespostaPerguntaInputDTO resposta = mock(RespostaPerguntaInputDTO.class);
        BindingResult resultado = mock(BindingResult.class);
        when(resultado.hasErrors()).thenReturn(false);
        when(service.registrarResposta(12L, resposta)).thenReturn(StatusSessao.EM_ANDAMENTO);

        assertEquals("redirect:/sessoes/12/pergunta",
                controller.registrarResposta(12L, resposta, resultado, mock(RedirectAttributes.class)));
        verify(service).registrarResposta(12L, resposta);
    }

    @Test
    void bloqueiaRespostaInvalidaEEncerraFluxoQuandoSessaoConcluida() {
        SessaoService service = mock(SessaoService.class);
        SessaoController controller = new SessaoController(service);
        RespostaPerguntaInputDTO resposta = mock(RespostaPerguntaInputDTO.class);
        BindingResult resultado = mock(BindingResult.class);
        RedirectAttributes redirects = mock(RedirectAttributes.class);
        when(resultado.hasErrors()).thenReturn(true);
        when(resultado.getAllErrors()).thenReturn(List.of(new ObjectError("respostaForm", "Campo obrigatório")));

        assertEquals("redirect:/sessoes/12/pergunta",
                controller.registrarResposta(12L, resposta, resultado, redirects));
        verify(redirects).addFlashAttribute("erroSessao", "Campo obrigatório");
        verify(service, never()).registrarResposta(12L, resposta);

        when(resultado.hasErrors()).thenReturn(false);
        when(service.registrarResposta(12L, resposta)).thenReturn(StatusSessao.CONCLUIDA);
        assertEquals("redirect:/sessoes/resumo/12",
                controller.registrarResposta(12L, resposta, resultado, redirects));
    }
}
