package app.ritmo.aplicacaoritmo.infra.jobs;

import app.ritmo.aplicacaoritmo.services.AuditoriaRetencaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaRetentionJob {

    private final AuditoriaRetencaoService auditoriaRetencaoService;

    @Scheduled(cron = "0 0 3 * * *")
    public void removerAuditoriasExpiradas() {
        int removidas = auditoriaRetencaoService.excluirRegistrosExpirados();
        log.info("Rotina de retenção de auditoria concluída: {} registro(s) removido(s)", removidas);
    }
}
