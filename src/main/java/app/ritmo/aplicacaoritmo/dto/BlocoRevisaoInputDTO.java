package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlocoRevisaoInputDTO(
        @NotNull(message = "Escolha a duração do bloco de revisão.")
        Integer duracaoMinutos,

        @NotEmpty(message = "Selecione ao menos um tópico com revisão pendente.")
        @Size(max = 6, message = "Essa duração permite selecionar no máximo 6 tópicos.")
        List<@NotNull @Positive Long> revisaoIds
) {
}
