package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlocoInputDTO(
        @NotNull(message = "Selecione uma disciplina.")
        @Positive(message = "A disciplina selecionada é inválida.")
        Long disciplinaId,

        @NotNull(message = "Escolha a duração do bloco.")
        Integer duracaoMinutos,

        @NotEmpty(message = "Selecione pelo menos um tópico para montar o bloco.")
        @Size(max = 6, message = "Essa duração permite selecionar no máximo 6 tópicos.")
        List<@NotNull @Positive Long> topicoIds
) {
}