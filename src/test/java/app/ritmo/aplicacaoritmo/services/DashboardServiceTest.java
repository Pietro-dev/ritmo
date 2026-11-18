package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.PeriodoDashboard;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.RevisaoObrigatoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import app.ritmo.aplicacaoritmo.repositories.projections.DesempenhoProjection;
import app.ritmo.aplicacaoritmo.repositories.projections.ResumoSessaoProjection;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void agregaMetricasApenasParaUsuarioAutenticadoEDataAtual() {
        Long usuarioId = 42L;
        var securityUtils = mock(SecurityUtils.class);
        var sessoes = mock(SessaoEstudoRepository.class);
        var revisoes = mock(RevisaoObrigatoriaRepository.class);
        var resumoSessoes = mock(ResumoSessaoProjection.class);
        var desempenho = mock(DesempenhoProjection.class);
        Usuario usuario = new Usuario(
                "Estudante", "estudante@example.com", "hash",
                Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, LocalDateTime.now()
        );
        usuario.setId(usuarioId);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        var inicioPeriodo = LocalDate.of(2026, 3, 6).atStartOfDay();
        var fimPeriodo = LocalDate.of(2026, 4, 5).atStartOfDay();
        when(sessoes.resumirSessoesConcluidas(usuarioId, inicioPeriodo, fimPeriodo))
                .thenReturn(resumoSessoes);
        when(resumoSessoes.getSessoesConcluidas()).thenReturn(3L);
        when(resumoSessoes.getMinutosPlanejados()).thenReturn(45L);
        when(sessoes.desempenhoPorDisciplina(usuarioId, inicioPeriodo, fimPeriodo))
                .thenReturn(List.of(desempenho));
        when(sessoes.desempenhoPorTopico(usuarioId, inicioPeriodo, fimPeriodo))
                .thenReturn(List.of(desempenho));
        when(desempenho.getCategoria()).thenReturn("Matemática");
        when(desempenho.getDisciplina()).thenReturn("Matemática");
        when(desempenho.getTotal()).thenReturn(5L);
        when(desempenho.getFaceis()).thenReturn(2L);
        when(desempenho.getDificeis()).thenReturn(2L);
        when(desempenho.getNaoLembrei()).thenReturn(1L);
        when(revisoes.countByUsuario_IdAndConcluidaEmIsNull(usuarioId)).thenReturn(4L);
        when(revisoes.countByUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNullAndAgendadaParaLessThanEqual(
                usuarioId, LocalDate.of(2026, 4, 4)
        )).thenReturn(2L);

        DashboardService service = new DashboardService(
                securityUtils,
                sessoes,
                revisoes,
                Clock.fixed(Instant.parse("2026-04-04T00:00:00Z"), ZoneOffset.UTC)
        );

        var resultado = service.carregarResumo(PeriodoDashboard.TRINTA_DIAS);

        assertEquals(3L, resultado.sessoesConcluidas());
        assertEquals(45L, resultado.minutosPlanejados());
        assertEquals(5L, resultado.perguntasAvaliadas());
        assertEquals(4L, resultado.revisoesPendentes());
        assertEquals(2L, resultado.revisoesDisponiveis());
        assertEquals("Matemática", resultado.porTopico().getFirst().categoria());
        verify(sessoes).desempenhoPorDisciplina(usuarioId, inicioPeriodo, fimPeriodo);
        verify(sessoes).desempenhoPorTopico(usuarioId, inicioPeriodo, fimPeriodo);
    }
}
