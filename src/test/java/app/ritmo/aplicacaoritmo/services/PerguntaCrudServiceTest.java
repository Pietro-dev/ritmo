package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.domain.Pergunta;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Topico;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.PerguntaInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerguntaCrudServiceTest {

    private static final Long USUARIO_ID = 42L;
    private static final Long DISCIPLINA_ID = 9L;
    private static final Long TOPICO_ID = 12L;
    private static final Long PERGUNTA_ID = 18L;

    private SecurityUtils securityUtils;
    private PerguntaRepository perguntaRepository;
    private TopicoRepository topicoRepository;
    private SessaoEstudoRepository sessaoRepository;
    private AuditoriaService auditoriaService;
    private PerguntaService service;
    private Usuario usuario;
    private Topico topico;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Estudante", "estudante@example.com", "hash",
                Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, LocalDateTime.now()
        );
        usuario.setId(USUARIO_ID);
        Disciplina disciplina = new Disciplina("matemática");
        disciplina.setId(DISCIPLINA_ID);
        disciplina.setUsuario(usuario);
        topico = new Topico("álgebra", disciplina);
        topico.setId(TOPICO_ID);
        securityUtils = mock(SecurityUtils.class);
        perguntaRepository = mock(PerguntaRepository.class);
        topicoRepository = mock(TopicoRepository.class);
        sessaoRepository = mock(SessaoEstudoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        when(sessaoRepository.existsByUsuario_IdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(
                TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(Optional.of(topico));
        service = new PerguntaService(
                perguntaRepository,
                topicoRepository,
                sessaoRepository,
                securityUtils,
                auditoriaService
        );
    }

    @Test
    void cadastraPerguntaNormalizadaERegistraAuditoria() {
        service.cadastrar(DISCIPLINA_ID, TOPICO_ID, new PerguntaInputDTO(" Enunciado ", " Gabarito "));

        var captor = org.mockito.ArgumentCaptor.forClass(Pergunta.class);
        verify(perguntaRepository).save(captor.capture());
        assertEquals("Enunciado", captor.getValue().getEnunciado());
        assertEquals("Gabarito", captor.getValue().getGabarito());
        assertEquals(topico, captor.getValue().getTopico());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.CRIAR_PERGUNTA,
                "Criou uma pergunta no tópico: álgebra"
        );
    }

    @Test
    void listaPerguntasEConsultaGabaritoParaEdicao() {
        Pergunta pergunta = pergunta();
        when(perguntaRepository
                .findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(
                        TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
                )).thenReturn(List.of(pergunta));
        when(perguntaRepository
                .findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(
                        PERGUNTA_ID, TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
                )).thenReturn(Optional.of(pergunta));

        var perguntas = service.listarPerguntasDoTopico(DISCIPLINA_ID, TOPICO_ID);
        var edicao = service.buscarParaEdicao(DISCIPLINA_ID, TOPICO_ID, PERGUNTA_ID);

        assertEquals("Enunciado", perguntas.getFirst().enunciado());
        assertEquals("Gabarito", perguntas.getFirst().gabarito());
        assertEquals("Enunciado", edicao.enunciado());
        assertEquals("Gabarito", edicao.gabarito());
    }

    @Test
    void atualizaPerguntaERegistraAuditoria() {
        Pergunta pergunta = pergunta();
        when(perguntaRepository
                .findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(
                        PERGUNTA_ID, TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
                )).thenReturn(Optional.of(pergunta));

        service.atualizar(
                DISCIPLINA_ID,
                TOPICO_ID,
                PERGUNTA_ID,
                new PerguntaInputDTO("  Novo enunciado ", " Nova resposta ")
        );

        assertEquals("Novo enunciado", pergunta.getEnunciado());
        assertEquals("Nova resposta", pergunta.getGabarito());
        verify(perguntaRepository).save(pergunta);
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.ATUALIZAR_PERGUNTA,
                "Atualizou uma pergunta no tópico: álgebra"
        );
    }

    @Test
    void excluiPerguntaERegistraAuditoria() {
        Pergunta pergunta = pergunta();
        when(perguntaRepository
                .findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(
                        PERGUNTA_ID, TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
                )).thenReturn(Optional.of(pergunta));

        service.excluir(DISCIPLINA_ID, TOPICO_ID, PERGUNTA_ID);

        verify(perguntaRepository).delete(pergunta);
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.EXCLUIR_PERGUNTA,
                "Excluiu uma pergunta do tópico: álgebra"
        );
    }

    @Test
    void retornaNaoEncontradoParaPerguntaDeOutroUsuario() {
        when(perguntaRepository
                .findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(
                        PERGUNTA_ID, TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
                )).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontrado.class,
                () -> service.buscarParaEdicao(DISCIPLINA_ID, TOPICO_ID, PERGUNTA_ID)
        );
    }

    private Pergunta pergunta() {
        Pergunta pergunta = new Pergunta("Enunciado", "Gabarito", topico);
        pergunta.setId(PERGUNTA_ID);
        return pergunta;
    }
}
