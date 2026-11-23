package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;

import java.time.LocalDateTime;

public record UsuarioAdminDTO(
        Long id,
        String nome,
        String email,
        Role role,
        StatusCadastro status,
        String motivoInativacao,
        LocalDateTime inativadoEm,
        LocalDateTime inativadoAte
) {
}
