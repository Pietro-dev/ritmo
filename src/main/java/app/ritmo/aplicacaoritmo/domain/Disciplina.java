package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Disciplina {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    public Disciplina(String nome) {
        this.nome = nome;
    }

    public Disciplina() {
    }
}
