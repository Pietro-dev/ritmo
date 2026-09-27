package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.Disciplina;

public record DisciplinaOutputDTO(Long id, String nome) {
    // metodo para mapear a entidade em DTO
    public static DisciplinaOutputDTO fromEntity(Disciplina disciplina) {
        return new DisciplinaOutputDTO(disciplina.getId(), disciplina.getNome());
    }
}
