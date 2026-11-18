package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditoriaRetencaoServiceTest {

    @Test
    void removeSomenteRegistrosAnterioresAoLimiteDeSeisMeses() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-11-17T15:00:00Z"),
                ZoneId.of("UTC")
        );
        AuditoriaRepository repository = mock(AuditoriaRepository.class);
        LocalDateTime limite = LocalDateTime.of(2026, 5, 17, 15, 0);
        when(repository.excluirAnterioresA(limite)).thenReturn(3);

        AuditoriaRetencaoService service = new AuditoriaRetencaoService(repository, clock);

        assertEquals(3, service.excluirRegistrosExpirados());
        verify(repository).excluirAnterioresA(limite);
    }
}
