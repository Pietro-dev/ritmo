package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaRetencaoService {

    private static final int MESES_RETENCAO = 6;

    private final AuditoriaRepository auditoriaRepository;
    private final Clock clock;

    @Transactional
    public int excluirRegistrosExpirados() {
        LocalDateTime limite = LocalDateTime.now(clock).minusMonths(MESES_RETENCAO);
        return auditoriaRepository.excluirAnterioresA(limite);
    }
}
