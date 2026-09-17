package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisciplinaInputDTO(
        @NotBlank(message = "O nome da disciplina é obrigatório!")
        @Size(max = 50, message = "O nome não deve exceder 50 caracteres!")
        String nome
) {
}
