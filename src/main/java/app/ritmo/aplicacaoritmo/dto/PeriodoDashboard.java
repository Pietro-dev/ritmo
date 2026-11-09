package app.ritmo.aplicacaoritmo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum PeriodoDashboard {
    SETE_DIAS("7 dias", 7),
    TRINTA_DIAS("30 dias", 30),
    NOVENTA_DIAS("90 dias", 90),
    TODO_PERIODO("Todo o período", null);

    private final String rotulo;
    private final Integer dias;

    PeriodoDashboard(String rotulo, Integer dias) {
        this.rotulo = rotulo;
        this.dias = dias;
    }

    public String getRotulo() {
        return rotulo;
    }

    public LocalDateTime inicio(LocalDate hoje) {
        return dias == null ? null : hoje.minusDays(dias - 1L).atStartOfDay();
    }

    public LocalDateTime fim(LocalDate hoje) {
        return dias == null ? null : hoje.plusDays(1).atStartOfDay();
    }
}
