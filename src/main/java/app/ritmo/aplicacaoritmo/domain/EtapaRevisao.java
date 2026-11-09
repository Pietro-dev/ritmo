package app.ritmo.aplicacaoritmo.domain;

import java.time.LocalDate;

public enum EtapaRevisao {
    HORAS_24 {
        @Override
        public LocalDate proximaData(LocalDate concluidaEm) {
            return concluidaEm.plusDays(1);
        }
    },
    DIAS_7 {
        @Override
        public LocalDate proximaData(LocalDate concluidaEm) {
            return concluidaEm.plusDays(7);
        }
    },
    DIAS_30 {
        @Override
        public LocalDate proximaData(LocalDate concluidaEm) {
            return concluidaEm.plusDays(30);
        }
    },
    MESES_2 {
        @Override
        public LocalDate proximaData(LocalDate concluidaEm) {
            return concluidaEm.plusMonths(2);
        }
    };

    public abstract LocalDate proximaData(LocalDate concluidaEm);

    public EtapaRevisao proximaEtapa() {
        return switch (this) {
            case HORAS_24 -> DIAS_7;
            case DIAS_7 -> DIAS_30;
            case DIAS_30 -> MESES_2;
            case MESES_2 -> null;
        };
    }

    public String getDescricao() {
        return switch (this) {
            case HORAS_24 -> "1 dia";
            case DIAS_7 -> "7 dias";
            case DIAS_30 -> "30 dias";
            case MESES_2 -> "2 meses";
        };
    }
}
