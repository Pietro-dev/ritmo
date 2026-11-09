package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record ExclusaoContaInputDTO(
        @NotBlank(message = "Informe sua senha atual.")
        String senhaAtual,
        @AssertTrue(message = "Confirme que deseja excluir sua conta.")
        boolean confirmarExclusao
) {
}
