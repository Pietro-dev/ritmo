package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Topico;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.DisciplinaInputDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisciplinaServiceTest {

    private static final Long USUARIO_ID = 42L;
    private static final Long DISCIPLINA_ID = 9L;

    private SecurityUtils securityUtils;
    private DisciplinaRepository disciplinaRepository;
    private TopicoRepository topicoRepository;
    private PerguntaRepository perguntaRepository;
    private AuditoriaService auditoriaService;
    private DisciplinaService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                "Estudante", "estudante@example.com", "hash",
                Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, LocalDateTime.now()
        );
        usuario.setId(USUARIO_ID);
        securityUtils = mock(SecurityUtils.class);
        disciplinaRepository = mock(DisciplinaRepository.class);
        topicoRepository = mock(TopicoRepository.class);
        perguntaRepository = mock(PerguntaRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        service = new DisciplinaService(
                disciplinaRepository,
                securityUtils,
                auditoriaService,
                topicoRepository,
                perguntaRepository
        );
    }

    @Test
    void cadastraDisciplinaNormalizandoNomeERegistrandoAuditoria() {
        service.cadastrar(new DisciplinaInputDTO("  MATEMÁTICA  "));

        var captor = org.mockito.ArgumentCaptor.forClass(Disciplina.class);
        verify(disciplinaRepository).save(captor.capture());
        assertEquals("matemática", captor.getValue().getNome());
        assertEquals(usuario, captor.getValue().getUsuario());
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.CRIAR_DISCIPLINA,
                "Criou a disciplina: matemática"
        );
    }

    @Test
    void impedeCadastroDeDisciplinaDuplicada() {
        when(disciplinaRepository.existsByNomeAndUsuarioId("matemática", USUARIO_ID))
                .thenReturn(true);

        assertThrows(
                EntidadeDuplicadaException.class,
                () -> service.cadastrar(new DisciplinaInputDTO(" Matemática "))
        );

        verify(disciplinaRepository, never()).save(any(Disciplina.class));
    }

    @Test
    void listaEDetalhaDisciplinasDoUsuarioComNomeCapitalizado() {
        Disciplina disciplina = disciplina("matemática");
        when(disciplinaRepository.findByUsuarioId(USUARIO_ID)).thenReturn(List.of(disciplina));
        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.of(disciplina));

        assertEquals("Matemática", service.listarDoUsuarioLogado().getFirst().nome());
        assertEquals("Matemática", service.buscarPeloIdDoUsuario(DISCIPLINA_ID).nome());
        verify(disciplinaRepository).findByUsuarioId(USUARIO_ID);
    }

    @Test
    void naoExpõeDisciplinaDeOutroUsuario() {
        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontrado.class,
                () -> service.buscarPeloIdDoUsuario(DISCIPLINA_ID)
        );
    }

    @Test
    void atualizaNomeERejeitaDuplicidade() {
        Disciplina disciplina = disciplina("matemática");
        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.of(disciplina));

        service.atualizar(DISCIPLINA_ID, new DisciplinaInputDTO(" Física "));

        assertEquals("física", disciplina.getNome());
        verify(disciplinaRepository).existsByNomeAndUsuarioIdAndIdNot(
                "física", USUARIO_ID, DISCIPLINA_ID
        );
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.ATUALIZAR_DISCIPLINA,
                "Alterou a disciplina de matemática para física"
        );

        when(disciplinaRepository.existsByNomeAndUsuarioIdAndIdNot(
                "química", USUARIO_ID, DISCIPLINA_ID
        )).thenReturn(true);
        assertThrows(
                EntidadeDuplicadaException.class,
                () -> service.atualizar(DISCIPLINA_ID, new DisciplinaInputDTO("Química"))
        );
    }

    @Test
    void excluiPerguntasETopicosAntesDaDisciplina() {
        Disciplina disciplina = disciplina("matemática");
        Topico topico = new Topico("álgebra", disciplina);
        when(disciplinaRepository.findByIdAndUsuarioId(DISCIPLINA_ID, USUARIO_ID))
                .thenReturn(Optional.of(disciplina));
        when(topicoRepository.findByDisciplina_IdAndDisciplina_Usuario_IdOrderByNomeAsc(
                DISCIPLINA_ID, USUARIO_ID
        )).thenReturn(List.of(topico));

        service.excluir(DISCIPLINA_ID);

        verify(perguntaRepository).deleteByTopico_Id(null);
        verify(topicoRepository).deleteAll(List.of(topico));
        verify(disciplinaRepository).delete(disciplina);
        verify(auditoriaService).registrar(
                "estudante@example.com",
                AcaoAuditoria.EXCLUIR_DISCIPLINA,
                "Excluiu a disciplina: matemática"
        );
    }

    private Disciplina disciplina(String nome) {
        Disciplina disciplina = new Disciplina(nome);
        disciplina.setId(DISCIPLINA_ID);
        disciplina.setUsuario(usuario);
        return disciplina;
    }
}
