package app.ritmo.aplicacaoritmo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EtapaRevisaoTest {

    @Test
    void agendaAsQuatroEtapasObrigatoriasEmSequencia() {
        LocalDate conclusao = LocalDate.of(2026, 11, 9);

        assertEquals(conclusao.plusDays(1), EtapaRevisao.HORAS_24.proximaData(conclusao));
        assertEquals(EtapaRevisao.DIAS_7, EtapaRevisao.HORAS_24.proximaEtapa());
        assertEquals(conclusao.plusDays(7), EtapaRevisao.DIAS_7.proximaData(conclusao));
        assertEquals(EtapaRevisao.DIAS_30, EtapaRevisao.DIAS_7.proximaEtapa());
        assertEquals(conclusao.plusDays(30), EtapaRevisao.DIAS_30.proximaData(conclusao));
        assertEquals(EtapaRevisao.MESES_2, EtapaRevisao.DIAS_30.proximaEtapa());
        assertEquals(conclusao.plusMonths(2), EtapaRevisao.MESES_2.proximaData(conclusao));
    }

    @Test
    void naoAgendaNovasRevisoesObrigatoriasDepoisDaEtapaBimestral() {
        assertNull(EtapaRevisao.MESES_2.proximaEtapa());
    }
}
