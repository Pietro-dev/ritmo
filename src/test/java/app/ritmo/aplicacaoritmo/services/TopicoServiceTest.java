package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Topico;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.TopicoInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.DisciplinaRepository;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopicoServiceTest {

    private static final Long USUARIO_ID = 42L;
    private static final Long DISCIPLINA_ID = 9L;
    private static final Long TOPICO_ID = 12L;

    private SecurityUtils securityUtils;
    private DisciplinaRepository disciplinaRepository;
    private TopicoRepository topicoRepository;
    private PerguntaRepository perguntaRepository;
    private AuditoriaService auditoriaService;
    private TopicoService service;
    private Usuario usuario;
    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Estudante", "estudante@example.com", "hash",
                Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, LocalDateTime.now()
        );
        usuario.setId(USUARIO_ID);
        disciplina = new Disciplina("matemática");
        disciplina.setId(DISCIPLINA_ID);
        disciplina.setUsuario(usuario);
        securityUtils = mock(SecurityUtils.class);
        disciplinaRepository = mock(DisciplinaRepository.class);
        topicoRepository = mock(TopicoRepository.class);
        perguntaRepository = mock(PerguntaRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.of(disciplina));
        service = new TopicoService(
                securityUtils,
                disciplinaRepository,
                topicoRepository,
                auditoriaService,
                perguntaRepository
        );
    }

    @Test
    void cadastraTopicoNormalizadoERegistraAuditoria() {
        service.cadastrar(DISCIPLINA_ID, new TopicoInputDTO("  ÁLGEBRA  "));

        var captor = org.mockito.ArgumentCaptor.forClass(Topico.class);
        verify(topicoRepository).save(captor.capture());
        assertEquals("álgebra", captor.getValue().getNome());
        assertEquals(disciplina, captor.getValue().getDisciplina());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.CRIAR_TOPICO,
                "Tópico criado na niscipina: matemática"
        );
    }

    @Test
    void rejeitaTopicoDuplicadoOuDisciplinaDeOutroUsuario() {
        when(topicoRepository.existsByDisciplina_IdAndNomeIgnoreCase(DISCIPLINA_ID, "álgebra"))
                .thenReturn(true);
        assertThrows(
                EntidadeDuplicadaException.class,
                () -> service.cadastrar(DISCIPLINA_ID, new TopicoInputDTO("Álgebra"))
        );
        verify(topicoRepository, never()).save(any(Topico.class));

        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.empty());
        assertThrows(
                RecursoNaoEncontrado.class,
                () -> service.cadastrar(DISCIPLINA_ID, new TopicoInputDTO("Cálculo"))
        );
    }

    @Test
    void listaEConsultaTopicosComContagemDePerguntas() {
        Topico topico = topico("álgebra");
        when(topicoRepository.findByDisciplina_IdAndDisciplina_Usuario_IdOrderByNomeAsc(
                DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(List.of(topico));
        when(topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(
                TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(Optional.of(topico));
        when(perguntaRepository.countByTopico_Id(TOPICO_ID)).thenReturn(4L);

        assertEquals("Álgebra", service.listarTopicosDaDisciplina(DISCIPLINA_ID).getFirst().nome());
        assertEquals(4L, service.listarTopicosDaDisciplina(DISCIPLINA_ID).getFirst().quantidadePerguntas());
        assertEquals("Álgebra", service.buscarPeloId(DISCIPLINA_ID, TOPICO_ID).nome());
        assertThrows(
                RecursoNaoEncontrado.class,
                () -> service.buscarPeloId(DISCIPLINA_ID, 99L)
        );
    }

    @Test
    void atualizaTopicoEImpedeDuplicidade() {
        Topico topico = topico("álgebra");
        when(topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(
                TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(Optional.of(topico));

        service.atualizar(DISCIPLINA_ID, TOPICO_ID, new TopicoInputDTO("  Geometria "));
        assertEquals("geometria", topico.getNome());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.ATUALIZAR_TOPICO,
                "Atualizou um tópico da disciplina: matemática (nome anterior: álgebra)"
        );

        when(topicoRepository.existsByDisciplina_IdAndNomeIgnoreCaseAndIdNot(
                DISCIPLINA_ID, "cálculo", TOPICO_ID
        )).thenReturn(true);
        assertThrows(
                EntidadeDuplicadaException.class,
                () -> service.atualizar(DISCIPLINA_ID, TOPICO_ID, new TopicoInputDTO("Cálculo"))
        );
    }

    @Test
    void excluiPerguntasAntesDoTopico() {
        Topico topico = topico("álgebra");
        when(topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(
                TOPICO_ID, DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(Optional.of(topico));

        service.excluir(DISCIPLINA_ID, TOPICO_ID);

        verify(perguntaRepository).deleteByTopico_Id(TOPICO_ID);
        verify(topicoRepository).delete(topico);
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.EXCLUIR_TOPICO,
                "Excluiu um tópico da disciplina: matemática"
        );
    }

    private Topico topico(String nome) {
        Topico topico = new Topico(nome, disciplina);
        topico.setId(TOPICO_ID);
        return topico;
    }
}
