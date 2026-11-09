package app.ritmo.aplicacaoritmo.repositories.projections;

import java.time.LocalDateTime;

public interface AuditoriaAdminProjection {
    LocalDateTime getDataHora();

    String getAcao();
}
