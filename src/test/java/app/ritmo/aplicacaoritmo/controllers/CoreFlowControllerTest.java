package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoOutputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoRevisaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.DisciplinaInputDTO;
import app.ritmo.aplicacaoritmo.dto.DisciplinaOutputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaOutputDTO;
import app.ritmo.aplicacaoritmo.dto.TopicoInputDTO;
import app.ritmo.aplicacaoritmo.dto.TopicoOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.services.DisciplinaService;
import app.ritmo.aplicacaoritmo.services.PerguntaService;
import app.ritmo.aplicacaoritmo.services.RevisaoService;
import app.ritmo.aplicacaoritmo.services.SessaoService;
import app.ritmo.aplicacaoritmo.services.TopicoService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreFlowControllerTest {

    @Test
    void disciplinaListaCriaEditaEExclui() {
        DisciplinaService service = mock(DisciplinaService.class);
        DisciplinaController controller = new DisciplinaController(service);
        DisciplinaOutputDTO output = mock(DisciplinaOutputDTO.class);
        when(service.listarDoUsuarioLogado()).thenReturn(List.of(output));
        DisciplinaInputDTO input = mock(DisciplinaInputDTO.class);

        assertEquals("disciplinas/lista", controller.listar(new ExtendedModelMap()));

        BindingResult invalido = bindingResult(true);
        assertEquals("disciplinas/lista", controller.cadastrar(input, invalido, new ExtendedModelMap()));
        verify(service, never()).cadastrar(input);

        BindingResult valido = bindingResult(false);
        assertEquals("redirect:/disciplinas", controller.cadastrar(input, valido, new ExtendedModelMap()));
        doThrow(new EntidadeDuplicadaException("Duplicada")).when(service).cadastrar(input);
        assertEquals("disciplinas/lista", controller.cadastrar(input, valido, new ExtendedModelMap()));
        verify(valido).rejectValue("nome", "duplicado", "Duplicada");

        when(service.buscarPeloIdDoUsuario(5L)).thenReturn(output);
        assertEquals("disciplinas/editar", controller.exibirFormularioEdicao(5L, new ExtendedModelMap()));
        assertEquals("disciplinas/editar", controller.atualizar(5L, input, invalido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas", controller.atualizar(5L, input, valido, new ExtendedModelMap()));
        doThrow(new NegocioException("Duplicada")).when(service).atualizar(5L, input);
        assertEquals("disciplinas/editar", controller.atualizar(5L, input, valido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas", controller.excluir(5L));
    }

    @Test
    void topicoListaCriaEditaEExclui() {
        TopicoService topicos = mock(TopicoService.class);
        DisciplinaService disciplinas = mock(DisciplinaService.class);
        TopicoController controller = new TopicoController(topicos, disciplinas);
        when(topicos.listarTopicosDaDisciplina(3L)).thenReturn(List.of(mock(TopicoOutputDTO.class)));
        when(topicos.buscarPeloId(3L, 7L)).thenReturn(mock(TopicoOutputDTO.class));
        when(disciplinas.buscarPeloIdDoUsuario(3L)).thenReturn(mock(DisciplinaOutputDTO.class));
        TopicoInputDTO input = mock(TopicoInputDTO.class);

        assertEquals("topicos/lista", controller.listar(3L, new ExtendedModelMap()));
        BindingResult invalido = bindingResult(true);
        assertEquals("topicos/lista", controller.cadastrar(3L, input, invalido, new ExtendedModelMap()));

        BindingResult valido = bindingResult(false);
        assertEquals("redirect:/disciplinas/3/topicos",
                controller.cadastrar(3L, input, valido, new ExtendedModelMap()));
        doThrow(new EntidadeDuplicadaException("Duplicado")).when(topicos).cadastrar(3L, input);
        assertEquals("topicos/lista", controller.cadastrar(3L, input, valido, new ExtendedModelMap()));
        verify(valido).rejectValue("nome", "duplicado", "Duplicado");

        assertEquals("topicos/editar",
                controller.exibirFormularioEdicao(3L, 7L, new ExtendedModelMap()));
        assertEquals("topicos/editar",
                controller.atualizar(3L, 7L, input, invalido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas/3/topicos",
                controller.atualizar(3L, 7L, input, valido, new ExtendedModelMap()));
        doThrow(new EntidadeDuplicadaException("Duplicado"))
                .when(topicos).atualizar(3L, 7L, input);
        assertEquals("topicos/editar",
                controller.atualizar(3L, 7L, input, valido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas/3/topicos", controller.excluir(3L, 7L));
    }

    @Test
    void perguntaListaCriaEditaEExclui() {
        PerguntaService perguntas = mock(PerguntaService.class);
        TopicoService topicos = mock(TopicoService.class);
        DisciplinaService disciplinas = mock(DisciplinaService.class);
        PerguntaController controller = new PerguntaController(perguntas, topicos, disciplinas);
        when(disciplinas.buscarPeloIdDoUsuario(2L)).thenReturn(mock(DisciplinaOutputDTO.class));
        when(topicos.buscarPeloId(2L, 4L)).thenReturn(mock(TopicoOutputDTO.class));
        when(perguntas.listarPerguntasDoTopico(2L, 4L))
                .thenReturn(List.of(mock(PerguntaOutputDTO.class)));
        PerguntaInputDTO input = mock(PerguntaInputDTO.class);

        assertEquals("perguntas/lista", controller.listar(2L, 4L, new ExtendedModelMap()));
        BindingResult invalido = bindingResult(true);
        assertEquals("perguntas/lista",
                controller.cadastrar(2L, 4L, input, invalido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas/2/topicos/4/perguntas",
                controller.cadastrar(2L, 4L, input, bindingResult(false), new ExtendedModelMap()));

        when(perguntas.buscarParaEdicao(2L, 4L, 9L)).thenReturn(input);
        assertEquals("perguntas/editar",
                controller.exibirFormularioEdicao(2L, 4L, 9L, new ExtendedModelMap()));
        assertEquals("perguntas/editar",
                controller.atualizar(2L, 4L, 9L, input, invalido, new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas/2/topicos/4/perguntas",
                controller.atualizar(2L, 4L, 9L, input, bindingResult(false), new ExtendedModelMap()));
        assertEquals("redirect:/disciplinas/2/topicos/4/perguntas",
                controller.excluir(2L, 4L, 9L));
    }

    @Test
    void configuracaoDoBlocoCarregaContextoValidaECriaSessao() {
        DisciplinaService disciplinas = mock(DisciplinaService.class);
        TopicoService topicos = mock(TopicoService.class);
        SessaoService sessoes = mock(SessaoService.class);
        BlocoController controller = new BlocoController(disciplinas, topicos, sessoes);
        DisciplinaOutputDTO disciplina = mock(DisciplinaOutputDTO.class);
        when(disciplina.id()).thenReturn(1L);
        when(disciplinas.listarDoUsuarioLogado()).thenReturn(List.of(disciplina));
        TopicoOutputDTO topico = mock(TopicoOutputDTO.class);
        when(topicos.listarTopicosDaDisciplina(1L)).thenReturn(List.of(topico));

        var model = new ExtendedModelMap();
        assertEquals("blocos/configuracao", controller.exibirFormulario(1L, model));
        verify(topicos).listarTopicosDaDisciplina(1L);
        assertEquals("blocos/configuracao",
                controller.exibirFormulario(99L, new ExtendedModelMap()));

        BlocoInputDTO input = mock(BlocoInputDTO.class);
        when(input.disciplinaId()).thenReturn(1L);
        assertEquals("blocos/configuracao",
                controller.configurar(input, bindingResult(true), new ExtendedModelMap()));
        when(sessoes.criar(input)).thenReturn(14L);
        assertEquals("redirect:/sessoes/14",
                controller.configurar(input, bindingResult(false), new ExtendedModelMap()));
        doThrow(new NegocioException("Bloco inválido")).when(sessoes).criar(input);
        var erro = new ExtendedModelMap();
        assertEquals("blocos/configuracao", controller.configurar(input, bindingResult(false), erro));
        assertEquals("Bloco inválido", erro.getAttribute("erroConfiguracao"));
    }

    @Test
    void criacaoDeBlocoDeRevisaoTrataValidacaoSucessoEErroDeRegra() {
        RevisaoService service = mock(RevisaoService.class);
        RevisaoController controller = new RevisaoController(service);
        when(service.listarRevisoes()).thenReturn(List.of());
        assertEquals("revisoes/lista", controller.listar(new ExtendedModelMap()));

        BlocoRevisaoInputDTO input = mock(BlocoRevisaoInputDTO.class);
        assertEquals("revisoes/lista",
                controller.criarBloco(input, bindingResult(true), new ExtendedModelMap()));
        when(service.criarBloco(input)).thenReturn(18L);
        assertEquals("redirect:/sessoes/18",
                controller.criarBloco(input, bindingResult(false), new ExtendedModelMap()));
        doThrow(new NegocioException("Revisão indisponível")).when(service).criarBloco(input);
        var erro = new ExtendedModelMap();
        assertEquals("revisoes/lista", controller.criarBloco(input, bindingResult(false), erro));
        assertEquals("Revisão indisponível", erro.getAttribute("erroRevisao"));
    }

    private BindingResult bindingResult(boolean hasErrors) {
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(hasErrors);
        return result;
    }
}
