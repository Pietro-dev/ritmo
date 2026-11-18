package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoOutputDTO;
import app.ritmo.aplicacaoritmo.dto.TopicoOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlocoServiceTest {

    private TopicoService topicoService;
    private BlocoService blocoService;

    @BeforeEach
    void setUp() {
        topicoService = mock(TopicoService.class);
        blocoService = new BlocoService(topicoService);
    }

    @Test
    void configuraBlocoDeCincoMinutosComUmTopico() {
        when(topicoService.listarTopicosDaDisciplina(10L))
                .thenReturn(List.of(topico(1L, "Álgebra", 5)));

        BlocoOutputDTO bloco = blocoService.configurar(
                new BlocoInputDTO(10L, 5, List.of(1L))
        );

        assertEquals(5, bloco.quantidadePerguntas());
        assertEquals(5, bloco.topicos().get(0).perguntasNoBloco());
    }

    @Test
    void distribuiPerguntasDeQuinzeMinutosEntreDoisTopicos() {
        when(topicoService.listarTopicosDaDisciplina(10L))
                .thenReturn(List.of(
                        topico(1L, "Álgebra", 8),
                        topico(2L, "Geometria", 8)
                ));

        BlocoOutputDTO bloco = blocoService.configurar(
                new BlocoInputDTO(10L, 15, List.of(1L, 2L))
        );

        assertEquals(15, bloco.quantidadePerguntas());
        assertEquals(
                List.of(8, 7),
                bloco.topicos().stream()
                        .map(BlocoOutputDTO.TopicoConfiguradoDTO::perguntasNoBloco)
                        .toList()
        );
    }

    @Test
    void configuraTrintaMinutosComSeisTopicos() {
        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        List<TopicoOutputDTO> topicos = ids.stream()
                .map(id -> topico(id, "Tópico " + id, 5))
                .toList();
        when(topicoService.listarTopicosDaDisciplina(10L)).thenReturn(topicos);

        BlocoOutputDTO bloco = blocoService.configurar(
                new BlocoInputDTO(10L, 30, ids)
        );

        assertEquals(30, bloco.quantidadePerguntas());
        assertEquals(6, bloco.topicos().size());
        assertEquals(
                List.of(5, 5, 5, 5, 5, 5),
                bloco.topicos().stream()
                        .map(BlocoOutputDTO.TopicoConfiguradoDTO::perguntasNoBloco)
                        .toList()
        );
    }

    @Test
    void rejeitaDuracaoNaoPermitida() {
        assertThrows(
                NegocioException.class,
                () -> blocoService.configurar(new BlocoInputDTO(10L, 10, List.of(1L)))
        );
    }

    @Test
    void rejeitaQuantidadeDeTopicosAcimaDoLimiteDaDuracao() {
        assertThrows(
                NegocioException.class,
                () -> blocoService.configurar(
                        new BlocoInputDTO(10L, 5, List.of(1L, 2L))
                )
        );
    }

    @Test
    void rejeitaTopicoSemQuantidadeMinimaDePerguntas() {
        when(topicoService.listarTopicosDaDisciplina(10L))
                .thenReturn(List.of(
                        topico(1L, "Álgebra", 8),
                        topico(2L, "Geometria", 7)
                ));

        NegocioException excecao = assertThrows(
                NegocioException.class,
                () -> blocoService.configurar(
                        new BlocoInputDTO(10L, 15, List.of(1L, 2L))
                )
        );

        assertTrue(excecao.getMessage().contains("bloco de 15 minutos com 2 tópicos"));
        assertTrue(excecao.getMessage().contains("pelo menos 8 perguntas disponíveis"));
        assertTrue(excecao.getMessage().contains("Geometria (7 perguntas disponíveis)"));
    }

    @Test
    void rejeitaTopicoQueNaoPertenceADisciplinaConsultada() {
        when(topicoService.listarTopicosDaDisciplina(10L))
                .thenReturn(List.of(topico(1L, "Álgebra", 5)));

        assertThrows(
                NegocioException.class,
                () -> blocoService.configurar(
                        new BlocoInputDTO(10L, 5, List.of(2L))
                )
        );
    }

    @Test
    void rejeitaIdsDeTopicosDuplicados() {
        assertThrows(
                NegocioException.class,
                () -> blocoService.configurar(
                        new BlocoInputDTO(10L, 15, List.of(1L, 1L))
                )
        );
    }

    private TopicoOutputDTO topico(Long id, String nome, long perguntas) {
        return new TopicoOutputDTO(id, nome, perguntas);
    }
}
