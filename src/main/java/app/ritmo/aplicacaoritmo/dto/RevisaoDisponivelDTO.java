package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.EtapaRevisao;
import app.ritmo.aplicacaoritmo.domain.StatusSessao;

import java.time.LocalDate;

public record RevisaoDisponivelDTO(
        Long id,
        Long topicoId,
        String topicoNome,
        String disciplinaNome,
        EtapaRevisao etapa,
        LocalDate agendadaPara,
        int perguntasDisponiveis,
        boolean atrasada,
        Long sessaoRevisaoId,
        StatusSessao statusSessaoRevisao
) {
}
