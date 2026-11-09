package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.Autoavaliacao;
import jakarta.validation.constraints.NotNull;

public record AutoavaliacaoInputDTO(
        @NotNull(message = "Escolha como foi responder à pergunta.")
        Autoavaliacao autoavaliacao
) {
}
