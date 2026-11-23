package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import app.ritmo.aplicacaoritmo.infra.security.EmailHash;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import app.ritmo.aplicacaoritmo.infra.config.ClockConfig;

@Entity
@Table(name = "auditorias")
@Getter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = app.ritmo.aplicacaoritmo.infra.security.EmailEncryptionConverter.class)
    @Column(name = "usuario_email", nullable = false, length = 1024)
    private String usuarioEmail;

    @Column(name = "usuario_email_hash", length = 64)
    private String usuarioEmailHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private AcaoAuditoria acao;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public Auditoria(String usuarioEmail, AcaoAuditoria acao, String descricao) {
        this.usuarioEmail = usuarioEmail == null ? null : usuarioEmail.trim().toLowerCase();
        this.usuarioEmailHash = EmailHash.of(usuarioEmail);
        this.acao = acao;
        this.descricao = descricao;
        this.dataHora = ZonedDateTime.now(ClockConfig.ZONA_BRASIL).toLocalDateTime();
    }
}
