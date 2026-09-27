package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaOutputDTO(
        LocalDateTime dataHora,
        String usuarioEmail,
        AcaoAuditoria acao,
        String descricao
) {
    public static AuditoriaOutputDTO fromEntity(Auditoria auditoria) {
        return new AuditoriaOutputDTO(auditoria.getDataHora(),auditoria.getUsuarioEmail(), auditoria.getAcao(), auditoria.getDescricao()
        );
    }
}
