package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Auditoria;
import app.ritmo.aplicacaoritmo.dto.AuditoriaOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditoriaServiceTest {

    private AuditoriaRepository repository;
    private AuditoriaService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuditoriaRepository.class);
        service = new AuditoriaService(repository);
    }

    @Test
    void registraAcaoComEmailEDescricaoNormalizados() {
        service.registrar("  ALUNO@EXAMPLE.COM ", AcaoAuditoria.LOGIN, " Entrou no sistema ");

        var captor = org.mockito.ArgumentCaptor.forClass(Auditoria.class);
        verify(repository).save(captor.capture());
        assertEquals("aluno@example.com", captor.getValue().getUsuarioEmail());
        assertEquals("Entrou no sistema", captor.getValue().getDescricao());
        assertEquals(AcaoAuditoria.LOGIN, captor.getValue().getAcao());
    }

    @Test
    void rejeitaAcaoOuDescricaoObrigatoria() {
        assertThrows(
                NegocioException.class,
                () -> service.registrar("aluno@example.com", null, "Descrição")
        );
        assertThrows(
                NegocioException.class,
                () -> service.registrar("aluno@example.com", AcaoAuditoria.LOGIN, "  ")
        );
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Auditoria.class));
    }

    @Test
    void filtraAuditoriasNormalizandoEmailEIncluindoTodoODiaFinal() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 3);
        var pageable = PageRequest.of(0, 10);
        Auditoria auditoria = new Auditoria("aluno@example.com", AcaoAuditoria.LOGIN, "Login");
        when(repository.pesquisar(
                "aluno@example.com",
                inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(auditoria), pageable, 1));

        var resultado = service.pesquisar(" Aluno@Example.com ", inicio, fim, pageable);

        assertEquals(1, resultado.getTotalElements());
        AuditoriaOutputDTO dto = resultado.getContent().getFirst();
        assertEquals("aluno@example.com", dto.usuarioEmail());
        assertEquals(AcaoAuditoria.LOGIN, dto.acao());
        verify(repository).pesquisar(
                "aluno@example.com",
                inicio.atStartOfDay(),
                fim.plusDays(1).atStartOfDay(),
                pageable
        );
    }

    @Test
    void pesquisaSemFiltrosERejeitaIntervaloInvertido() {
        var pageable = PageRequest.of(0, 5);
        when(repository.pesquisar(isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertEquals(0, service.pesquisar(" ", null, null, pageable).getTotalElements());
        assertThrows(
                NegocioException.class,
                () -> service.pesquisar(
                        null,
                        LocalDate.of(2026, 11, 4),
                        LocalDate.of(2026, 11, 3),
                        pageable
                )
        );
    }
}
