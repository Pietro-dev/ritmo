package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Pergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String gabarito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topico_id", nullable = false)
    private Topico topico;

    public Pergunta(String enunciado, String gabarito, Topico topico) {
        this.enunciado = enunciado;
        this.gabarito = gabarito;
        this.topico = topico;
    }

    public Pergunta() {
    }
}
