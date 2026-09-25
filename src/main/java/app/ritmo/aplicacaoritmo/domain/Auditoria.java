package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias")
@Getter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String usuarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private AcaoAuditoria acao;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public Auditoria(String usuarioEmail, AcaoAuditoria acao, String descricao) {
        this.usuarioEmail = usuarioEmail;
        this.acao = acao;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }
}
