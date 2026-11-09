package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilInputDTO(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String nome,
        String senhaAtual,
        String novaSenha,
        String confirmarNovaSenha
) {
}
