package app.ritmo.aplicacaoritmo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioInputDTO(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha,

        @AssertTrue(message = "Você deve aceitar os Termos de Uso e Política de Privacidade (LGPD).")
        boolean aceitouTermo
) {

}
