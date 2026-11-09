package app.ritmo.aplicacaoritmo.repositories.projections;

import java.time.LocalDateTime;

public interface AtividadeAdminProjection {
    LocalDateTime getPeriodo();

    Long getCadastros();

    Long getSessoes();
}
