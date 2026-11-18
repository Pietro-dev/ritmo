package app.ritmo.aplicacaoritmo.infra.jobs;

import app.ritmo.aplicacaoritmo.services.AuditoriaRetencaoService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditoriaRetentionJobTest {

    @Test
    void executaServicoDeRetencao() {
        AuditoriaRetencaoService service = mock(AuditoriaRetencaoService.class);
        when(service.excluirRegistrosExpirados()).thenReturn(2);

        new AuditoriaRetentionJob(service).removerAuditoriasExpiradas();

        verify(service).excluirRegistrosExpirados();
    }
}
