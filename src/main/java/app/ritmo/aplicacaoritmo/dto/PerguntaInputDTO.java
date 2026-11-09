package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotBlank;

public record PerguntaInputDTO(
        @NotBlank(message = "O enunciado é obrigatório.")
        String enunciado,

        @NotBlank(message = "O gabarito é obrigatório.")
        String gabarito
) {
}
