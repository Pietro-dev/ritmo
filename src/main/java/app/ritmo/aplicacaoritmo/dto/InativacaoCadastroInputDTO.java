package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InativacaoCadastroInputDTO(
        @NotBlank(message = "Informe o motivo da inativação.")
        @Size(max = 500, message = "O motivo pode ter até 500 caracteres.")
        String motivo
) {
}
