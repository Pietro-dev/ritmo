package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCadastro status;

    @Column(nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime termoAceitoEm;

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
