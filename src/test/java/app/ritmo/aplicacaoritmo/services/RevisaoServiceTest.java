package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.EtapaRevisao;
import app.ritmo.aplicacaoritmo.domain.ItemSessao;
import app.ritmo.aplicacaoritmo.domain.RevisaoObrigatoria;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.SessaoEstudo;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.domain.TipoSessao;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.BlocoRevisaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.RevisaoDisponivelDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.ItemSessaoRepository;
import app.ritmo.aplicacaoritmo.repositories.RevisaoObrigatoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevisaoServiceTest {

    private static final Long USUARIO_ID = 42L;
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 17);

    private SecurityUtils securityUtils;
    private RevisaoObrigatoriaRepository revisaoRepository;
    private SessaoEstudoRepository sessaoRepository;
    private ItemSessaoRepository itemSessaoRepository;
    private AuditoriaService auditoriaService;
    private RevisaoService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Estudante", "estudante@example.com", "hash",
                Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, HOJE.atStartOfDay()
        );
        usuario.setId(USUARIO_ID);
        securityUtils = mock(SecurityUtils.class);
        revisaoRepository = mock(RevisaoObrigatoriaRepository.class);
        sessaoRepository = mock(SessaoEstudoRepository.class);
        itemSessaoRepository = mock(ItemSessaoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        service = new RevisaoService(
                securityUtils,
                revisaoRepository,
                sessaoRepository,
                itemSessaoRepository,
                auditoriaService,
                Clock.fixed(Instant.parse("2026-11-17T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void listaRevisoesComQuantidadeAtrasoESessaoAssociada() {
        RevisaoObrigatoria atrasada = revisao(1L, 1L, HOJE.minusDays(1), EtapaRevisao.HORAS_24);
        SessaoEstudo sessaoRevisao = mock(SessaoEstudo.class);
        when(sessaoRevisao.getId()).thenReturn(90L);
        when(sessaoRevisao.getStatus()).thenReturn(StatusSessao.EM_ANDAMENTO);
        RevisaoObrigatoria futura = revisao(2L, 2L, HOJE.plusDays(1), EtapaRevisao.DIAS_7);
        futura.associarASessao(sessaoRevisao);
        when(revisaoRepository.findByUsuario_IdAndConcluidaEmIsNullOrderByAgendadaParaAsc(USUARIO_ID))
                .thenReturn(List.of(atrasada, futura));
        when(itemSessaoRepository.findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(20L, 1L))
                .thenReturn(List.of(item(1L, 1), item(1L, 2)));
        when(itemSessaoRepository.findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(20L, 2L))
                .thenReturn(List.of(item(2L, 1)));

        List<RevisaoDisponivelDTO> resultado = service.listarRevisoes();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).atrasada());
        assertEquals(2, resultado.get(0).perguntasDisponiveis());
        assertFalse(resultado.get(1).atrasada());
        assertEquals(90L, resultado.get(1).sessaoRevisaoId());
        assertEquals(StatusSessao.EM_ANDAMENTO, resultado.get(1).statusSessaoRevisao());
    }

    @Test
    void criaBlocoEAssociaRevisaoAosItensDaOrigem() {
        RevisaoObrigatoria revisao = revisao(10L, 1L, HOJE, EtapaRevisao.HORAS_24);
        when(revisaoRepository.findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                List.of(10L), USUARIO_ID
        )).thenReturn(List.of(revisao));
        when(itemSessaoRepository.findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(20L, 1L))
                .thenReturn(List.of(item(1L, 1), item(1L, 2), item(1L, 3), item(1L, 4), item(1L, 5)));
        SessaoEstudo sessaoRevisao = mock(SessaoEstudo.class);
        when(sessaoRevisao.getId()).thenReturn(90L);
        when(sessaoRepository.save(any(SessaoEstudo.class))).thenReturn(sessaoRevisao);

        Long sessaoId = service.criarBloco(new BlocoRevisaoInputDTO(5, List.of(10L)));

        assertEquals(90L, sessaoId);
        assertEquals(sessaoRevisao, revisao.getSessaoRevisao());
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(itemSessaoRepository).saveAll(captor.capture());
        assertEquals(5, captor.getValue().size());
        verify(revisaoRepository).saveAll(List.of(revisao));
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.CRIAR_BLOCO_REVISAO,
                "Criou a sessão de revisão #90 com duração de 5 minutos."
        );
    }

    @Test
    void rejeitaConfiguracoesDeBlocoDeRevisaoInvalidas() {
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(10, List.of(1L)))
        );
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(5, List.of()))
        );
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(5, List.of(1L, 1L)))
        );
        verify(revisaoRepository, never()).saveAll(any());
    }

    @Test
    void rejeitaRevisaoIndisponivelFuturaOuDuplicadaPorTopico() {
        when(revisaoRepository.findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                List.of(1L), USUARIO_ID
        )).thenReturn(List.of());
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(5, List.of(1L)))
        );

        RevisaoObrigatoria futura = revisao(2L, 2L, HOJE.plusDays(1), EtapaRevisao.HORAS_24);
        when(revisaoRepository.findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                List.of(2L), USUARIO_ID
        )).thenReturn(List.of(futura));
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(5, List.of(2L)))
        );

        List<RevisaoObrigatoria> topicoDuplicado = List.of(
                revisao(3L, 3L, HOJE, EtapaRevisao.HORAS_24),
                revisao(4L, 3L, HOJE, EtapaRevisao.DIAS_7)
        );
        when(revisaoRepository.findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                List.of(3L, 4L), USUARIO_ID
        )).thenReturn(topicoDuplicado);
        assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(15, List.of(3L, 4L)))
        );
    }

    @Test
    void rejeitaTopicosSemPerguntasSuficientesParaDuracao() {
        RevisaoObrigatoria revisao = revisao(1L, 1L, HOJE, EtapaRevisao.HORAS_24);
        when(revisaoRepository.findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                List.of(1L), USUARIO_ID
        )).thenReturn(List.of(revisao));
        when(itemSessaoRepository.findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(20L, 1L))
                .thenReturn(List.of(item(1L, 1), item(1L, 2)));

        NegocioException exception = assertThrows(
                NegocioException.class,
                () -> service.criarBloco(new BlocoRevisaoInputDTO(5, List.of(1L)))
        );

        assertTrue(exception.getMessage().contains("Álgebra"));
        verify(sessaoRepository, never()).save(any(SessaoEstudo.class));
    }

    @Test
    void agendaUmaRevisaoInicialPorTopicoEIgnoraSessaoDeRevisao() {
        SessaoEstudo sessao = mock(SessaoEstudo.class);
        when(sessao.getId()).thenReturn(20L);
        when(sessao.getTipo()).thenReturn(TipoSessao.ESTUDO);
        when(sessao.getUsuario()).thenReturn(usuario);
        when(itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(20L)).thenReturn(List.of(
                item(1L, 1),
                item(1L, 2),
                item(2L, 3),
                item(null, 4)
        ));

        service.agendarPrimeirasRevisoes(sessao, HOJE);

        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(revisaoRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.AGENDAR_REVISOES,
                "Agendou 2 revisões para a sessão #20."
        );

        SessaoEstudo sessaoRevisao = mock(SessaoEstudo.class);
        when(sessaoRevisao.getTipo()).thenReturn(TipoSessao.REVISAO);
        service.agendarPrimeirasRevisoes(sessaoRevisao, HOJE);
        verify(itemSessaoRepository, never()).findBySessao_IdOrderByPosicaoAsc(null);
    }

    @Test
    void concluiRevisaoEAgendaProximaEtapa() {
        SessaoEstudo sessaoRevisao = mock(SessaoEstudo.class);
        when(sessaoRevisao.getId()).thenReturn(30L);
        when(sessaoRevisao.getTipo()).thenReturn(TipoSessao.REVISAO);
        when(sessaoRevisao.getUsuario()).thenReturn(usuario);
        RevisaoObrigatoria revisao = revisao(7L, 7L, HOJE, EtapaRevisao.HORAS_24);
        revisao.associarASessao(sessaoRevisao);
        when(revisaoRepository.findBySessaoRevisao_IdAndConcluidaEmIsNull(30L))
                .thenReturn(List.of(revisao));

        service.agendarProximasRevisoes(sessaoRevisao, HOJE);

        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(revisaoRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        RevisaoObrigatoria proxima = (RevisaoObrigatoria) captor.getValue().getFirst();
        assertEquals(EtapaRevisao.DIAS_7, proxima.getEtapa());
        assertEquals(HOJE.plusDays(7), proxima.getAgendadaPara());
        assertEquals(HOJE, revisao.getConcluidaEm());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.CONCLUIR_REVISOES,
                "Concluiu 1 revisões na sessão #30."
        );
    }

    @Test
    void liberaRevisoesAssociadasAUmaSessaoInterrompida() {
        SessaoEstudo sessaoRevisao = mock(SessaoEstudo.class);
        when(sessaoRevisao.getId()).thenReturn(40L);
        when(sessaoRevisao.getTipo()).thenReturn(TipoSessao.REVISAO);
        RevisaoObrigatoria revisao = revisao(1L, 1L, HOJE, EtapaRevisao.HORAS_24);
        revisao.associarASessao(sessaoRevisao);
        when(revisaoRepository.findBySessaoRevisao_IdAndConcluidaEmIsNull(40L))
                .thenReturn(new ArrayList<>(List.of(revisao)));

        service.liberarRevisoesDaSessao(sessaoRevisao);

        assertTrue(revisao.getSessaoRevisao() == null);
    }

    private RevisaoObrigatoria revisao(
            Long revisaoId,
            Long topicoId,
            LocalDate data,
            EtapaRevisao etapa
    ) {
        SessaoEstudo origem = mock(SessaoEstudo.class);
        when(origem.getId()).thenReturn(20L);
        RevisaoObrigatoria revisao = new RevisaoObrigatoria(
                usuario, origem, topicoId, "Álgebra", "Matemática", etapa, data
        );
        ReflectionTestUtils.setField(revisao, "id", revisaoId);
        return revisao;
    }

    private ItemSessao item(Long topicoId, int posicao) {
        return new ItemSessao(
                null,
                null,
                posicao,
                topicoId,
                "Álgebra",
                "Matemática",
                "Pergunta " + posicao,
                "Gabarito " + posicao
        );
    }
}
