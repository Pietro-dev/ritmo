package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.ItemSessao;
import app.ritmo.aplicacaoritmo.domain.Autoavaliacao;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.Pergunta;
import app.ritmo.aplicacaoritmo.domain.SessaoEstudo;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.AutoavaliacaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoOutputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaSessaoDTO;
import app.ritmo.aplicacaoritmo.dto.RespostaPerguntaInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.ItemSessaoRepository;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessaoServiceTest {

    private static final Long SESSAO_ID = 12L;
    private static final Long USUARIO_ID = 42L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 9, 10, 0);

    private SecurityUtils securityUtils;
    private SessaoEstudoRepository sessaoRepository;
    private ItemSessaoRepository itemSessaoRepository;
    private PerguntaRepository perguntaRepository;
    private RevisaoService revisaoService;
    private SessaoService sessaoService;
    private SessaoEstudo sessao;
    private ItemSessao item;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                INICIO
        );
        usuario.setId(USUARIO_ID);

        sessao = new SessaoEstudo(usuario, 5);
        sessao.iniciarEm(INICIO);

        securityUtils = mock(SecurityUtils.class);
        sessaoRepository = mock(SessaoEstudoRepository.class);
        itemSessaoRepository = mock(ItemSessaoRepository.class);
        revisaoService = mock(RevisaoService.class);
        perguntaRepository = mock(PerguntaRepository.class);

        item = mock(ItemSessao.class);
        when(item.getId()).thenReturn(1L);
        when(item.getPosicao()).thenReturn(1);
        when(item.getEnunciadoSnapshot()).thenReturn("Enunciado");
        when(item.getGabaritoSnapshot()).thenReturn("Gabarito");
        when(item.getAutoavaliacao()).thenReturn(null);

        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        when(sessaoRepository.findByIdAndUsuario_Id(SESSAO_ID, USUARIO_ID))
                .thenReturn(Optional.of(sessao));
        when(itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(SESSAO_ID))
                .thenReturn(List.of(item));
        when(itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(null))
                .thenReturn(List.of(item));

        Clock clock = Clock.fixed(
                INICIO.plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        sessaoService = new SessaoService(
                mock(BlocoService.class),
                securityUtils,
                sessaoRepository,
                itemSessaoRepository,
                perguntaRepository,
                revisaoService,
                mock(AuditoriaService.class),
                clock
        );
    }

    @Test
    void escondeGabaritoAteQueRespostaSejaRegistrada() {
        when(item.getRespondidaEm()).thenReturn(null);

        PerguntaSessaoDTO semResposta = sessaoService.buscarPerguntaAtual(SESSAO_ID).orElseThrow();

        assertFalse(semResposta.respostaEnviada());
        assertNull(semResposta.gabarito());

        when(item.getRespondidaEm()).thenReturn(INICIO.plusSeconds(30));
        when(item.getRespostaEstudante()).thenReturn("Minha resposta");

        PerguntaSessaoDTO respondida = sessaoService.buscarPerguntaAtual(SESSAO_ID).orElseThrow();

        assertTrue(respondida.respostaEnviada());
        assertEquals("Minha resposta", respondida.respostaEstudante());
        assertEquals("Gabarito", respondida.gabarito());
    }

    @Test
    void registraRespostaNaPerguntaAtual() {
        when(item.getRespondidaEm()).thenReturn(null);

        StatusSessao status = sessaoService.registrarResposta(
                SESSAO_ID,
                new RespostaPerguntaInputDTO("Resposta do estudante")
        );

        assertEquals(StatusSessao.EM_ANDAMENTO, status);
        verify(item).registrarResposta("Resposta do estudante", INICIO.plusMinutes(1));
    }

    @Test
    void expirarPrazoLimpaTentativaEDevolveSessaoAoEstadoPendente() {
        Clock clock = Clock.fixed(
                INICIO.plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        sessaoService = new SessaoService(
                mock(BlocoService.class),
                securityUtils,
                sessaoRepository,
                itemSessaoRepository,
                perguntaRepository,
                revisaoService,
                mock(AuditoriaService.class),
                clock
        );

        Optional<PerguntaSessaoDTO> pergunta = sessaoService.buscarPerguntaAtual(SESSAO_ID);

        assertTrue(pergunta.isEmpty());
        assertEquals(StatusSessao.PENDENTE, sessao.getStatus());
        verify(item).reiniciarTentativa();
        verify(revisaoService).liberarRevisoesDaSessao(sessao);
    }

    @Test
    void concluiSessaoEAgendaRevisoesDepoisDaUltimaAutoavaliacao() {
        when(item.getRespondidaEm()).thenReturn(INICIO.plusSeconds(30));

        StatusSessao status = sessaoService.registrarAutoavaliacao(
                SESSAO_ID,
                new AutoavaliacaoInputDTO(Autoavaliacao.FACIL)
        );

        assertEquals(StatusSessao.CONCLUIDA, status);
        verify(item).registrarAutoavaliacao(
                Autoavaliacao.FACIL,
                INICIO.plusMinutes(1)
        );
        verify(revisaoService).agendarPrimeirasRevisoes(sessao, LocalDate.of(2026, 11, 9));
        verify(revisaoService).agendarProximasRevisoes(sessao, LocalDate.of(2026, 11, 9));
    }

    @Test
    void criaSessaoComSnapshotsDasPerguntasConfiguradas() {
        BlocoService blocoService = mock(BlocoService.class);
        BlocoInputDTO entrada = new BlocoInputDTO(5L, 5, List.of(7L));
        when(blocoService.configurar(entrada)).thenReturn(new BlocoOutputDTO(
                5,
                5,
                List.of(new BlocoOutputDTO.TopicoConfiguradoDTO(7L, "Álgebra", 6, 5))
        ));
        SessaoEstudo sessaoCriada = mock(SessaoEstudo.class);
        when(sessaoCriada.getId()).thenReturn(90L);
        when(sessaoRepository.save(any(SessaoEstudo.class))).thenReturn(sessaoCriada);
        AuditoriaService auditoria = mock(AuditoriaService.class);
        var topico = new app.ritmo.aplicacaoritmo.domain.Topico(
                "Álgebra",
                new app.ritmo.aplicacaoritmo.domain.Disciplina("Matemática")
        );
        topico.setId(7L);
        List<Pergunta> perguntas = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> new Pergunta("Pergunta " + i, "Resposta " + i, topico))
                .toList();
        when(perguntaRepository
                .findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(
                        7L, 5L, USUARIO_ID
                )).thenReturn(new ArrayList<>(perguntas));
        sessaoService = new SessaoService(
                blocoService,
                securityUtils,
                sessaoRepository,
                itemSessaoRepository,
                perguntaRepository,
                revisaoService,
                auditoria,
                Clock.fixed(INICIO.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        );

        assertEquals(90L, sessaoService.criar(entrada));

        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(itemSessaoRepository).saveAll(captor.capture());
        assertEquals(5, captor.getValue().size());
        verify(auditoria).registrar(
                "estudante@example.com",
                app.ritmo.aplicacaoritmo.domain.AcaoAuditoria.CRIAR_SESSAO,
                "Criou a sessão #90 com duração de 5 minutos."
        );
    }

    @Test
    void rejeitaCriacaoQuandoPerguntasDisponiveisSaoInsuficientes() {
        BlocoService blocoService = mock(BlocoService.class);
        BlocoInputDTO entrada = new BlocoInputDTO(5L, 5, List.of(7L));
        when(blocoService.configurar(entrada)).thenReturn(new BlocoOutputDTO(
                5,
                5,
                List.of(new BlocoOutputDTO.TopicoConfiguradoDTO(7L, "Álgebra", 4, 5))
        ));
        when(sessaoRepository.save(any(SessaoEstudo.class))).thenReturn(sessao);
        when(perguntaRepository
                .findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(
                        7L, 5L, USUARIO_ID
                )).thenReturn(List.of());
        sessaoService = new SessaoService(
                blocoService,
                securityUtils,
                sessaoRepository,
                itemSessaoRepository,
                perguntaRepository,
                revisaoService,
                mock(AuditoriaService.class),
                Clock.systemUTC()
        );

        assertThrows(NegocioException.class, () -> sessaoService.criar(entrada));
        verify(itemSessaoRepository, never()).saveAll(any());
    }

    @Test
    void iniciaSessaoPendenteERejeitaSessaoConcluida() {
        SessaoEstudo pendente = new SessaoEstudo(sessao.getUsuario(), 5);
        when(sessaoRepository.findByIdAndUsuario_Id(SESSAO_ID, USUARIO_ID))
                .thenReturn(Optional.of(pendente));

        sessaoService.iniciar(SESSAO_ID);

        assertEquals(StatusSessao.EM_ANDAMENTO, pendente.getStatus());
        verify(revisaoService, never()).agendarPrimeirasRevisoes(any(), any());

        pendente.concluirEm(LocalDateTime.now().plusMinutes(1));
        assertThrows(NegocioException.class, () -> sessaoService.iniciar(SESSAO_ID));
    }

    @Test
    void rejeitaRespostaVaziaOuMaiorQueOLimite() {
        assertThrows(
                NegocioException.class,
                () -> sessaoService.registrarResposta(
                        SESSAO_ID,
                        new RespostaPerguntaInputDTO("   ")
                )
        );
        assertThrows(
                NegocioException.class,
                () -> sessaoService.registrarResposta(
                        SESSAO_ID,
                        new RespostaPerguntaInputDTO("x".repeat(20001))
                )
        );
        verify(sessaoRepository, never()).findByIdAndUsuario_Id(SESSAO_ID, USUARIO_ID);
    }

    @Test
    void rejeitaAutoavaliacaoAntesDaResposta() {
        when(item.getRespondidaEm()).thenReturn(null);

        assertThrows(
                NegocioException.class,
                () -> sessaoService.registrarAutoavaliacao(
                        SESSAO_ID,
                        new AutoavaliacaoInputDTO(Autoavaliacao.FACIL)
                )
        );
    }
}
