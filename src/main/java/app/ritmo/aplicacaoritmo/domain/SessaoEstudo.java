package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessao_estudo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessaoEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSessao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSessao status;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "inicio_em")
    private LocalDateTime inicioEm;

    @Column(name = "fim_em")
    private LocalDateTime fimEm;

    public SessaoEstudo(Usuario usuario, int duracaoMinutos) {
        this(usuario, duracaoMinutos, TipoSessao.ESTUDO);
    }

    public SessaoEstudo(Usuario usuario, int duracaoMinutos, TipoSessao tipo) {
        this.usuario = usuario;
        this.duracaoMinutos = duracaoMinutos;
        this.tipo = tipo;
        this.status = StatusSessao.PENDENTE;
        this.criadaEm = LocalDateTime.now();
    }

    public void iniciarEm(LocalDateTime inicio) {
        if (status != StatusSessao.PENDENTE) {
            throw new IllegalStateException("Somente uma sessão pendente pode ser iniciada.");
        }
        if (inicio == null) {
            throw new IllegalArgumentException("A data de início é obrigatória.");
        }

        this.status = StatusSessao.EM_ANDAMENTO;
        this.inicioEm = inicio;
        this.fimEm = null;
    }

    public void concluirEm(LocalDateTime fim) {
        if (status != StatusSessao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma sessão em andamento pode ser concluída.");
        }
        if (fim == null || fim.isBefore(inicioEm)) {
            throw new IllegalArgumentException("A data de conclusão deve ser igual ou posterior ao início.");
        }

        this.status = StatusSessao.CONCLUIDA;
        this.fimEm = fim;
    }

    public void interromper() {
        if (status != StatusSessao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma sessão em andamento pode ser interrompida.");
        }

        this.status = StatusSessao.PENDENTE;
        this.inicioEm = null;
        this.fimEm = null;
    }

    public LocalDateTime prazoFinal() {
        return inicioEm == null ? null : inicioEm.plusMinutes(duracaoMinutos);
    }
}