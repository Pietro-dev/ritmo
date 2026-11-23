package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.Data;
import app.ritmo.aplicacaoritmo.infra.security.EmailHash;
import app.ritmo.aplicacaoritmo.infra.config.ClockConfig;
import java.time.ZonedDateTime;

import java.time.LocalDateTime;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Convert(converter = app.ritmo.aplicacaoritmo.infra.security.EmailEncryptionConverter.class)
    @Column(nullable = false, length = 1024, unique = true)
    private String email;

    @Column(name = "email_hash", length = 64, unique = true)
    private String emailHash;

    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCadastro status;

    @Column(nullable = false)
    private LocalDateTime dataCadastro =
            ZonedDateTime.now(ClockConfig.ZONA_BRASIL).toLocalDateTime();

    @Column(nullable = false)
    private LocalDateTime termoAceitoEm;

    @Column(name = "motivo_inativacao", length = 500)
    private String motivoInativacao;

    @Column(name = "inativado_em")
    private LocalDateTime inativadoEm;

    @Column(name = "inativado_ate")
    private LocalDateTime inativadoAte;

    @PrePersist
    @PreUpdate
    private void atualizarEmailHash() {
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
        this.emailHash = EmailHash.of(email);
    }

    public Usuario() {}

    public Usuario(String nome, String email, String senha, Role role, StatusCadastro status, LocalDateTime termoAceitoEm) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.status = status;
        this.termoAceitoEm = termoAceitoEm;
    }
}
