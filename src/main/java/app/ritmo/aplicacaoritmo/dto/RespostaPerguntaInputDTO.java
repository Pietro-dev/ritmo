package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RespostaPerguntaInputDTO(
        @NotBlank(message = "Escreva sua resposta antes de continuar.")
        @Size(max = 20000, message = "A resposta não pode ultrapassar 20.000 caracteres.")
        String resposta
) {
}
