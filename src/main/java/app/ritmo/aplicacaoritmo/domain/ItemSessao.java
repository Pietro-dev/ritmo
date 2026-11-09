package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_sessao",uniqueConstraints = {@UniqueConstraint(name = "uk_item_sessao_posicao",columnNames = {"sessao_id", "posicao"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoEstudo sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id")
    private Pergunta perguntaOriginal;

    @Column(name = "posicao", nullable = false)
    private int posicao;

    @Column(name = "enunciado_snapshot", nullable = false, columnDefinition = "TEXT")
    private String enunciadoSnapshot;

    @Column(name = "gabarito_snapshot", nullable = false, columnDefinition = "TEXT")
    private String gabaritoSnapshot;

    @Column(name = "topico_nome_snapshot", nullable = false, length = 50)
    private String topicoNomeSnapshot;

    @Column(name = "topico_id_snapshot")
    private Long topicoIdSnapshot;

    @Column(name = "disciplina_nome_snapshot", nullable = false, length = 255)
    private String disciplinaNomeSnapshot;

    @Column(name = "resposta_estudante", columnDefinition = "TEXT")
    private String respostaEstudante;

    @Enumerated(EnumType.STRING)
    @Column(name = "autoavaliacao", length = 20)
    private Autoavaliacao autoavaliacao;

    @Column(name = "respondida_em")
    private java.time.LocalDateTime respondidaEm;

    @Column(name = "autoavaliada_em")
    private java.time.LocalDateTime autoavaliadaEm;

    public ItemSessao(SessaoEstudo sessao, Pergunta pergunta, int posicao) {
        this.sessao = sessao;
        this.perguntaOriginal = pergunta;
        this.posicao = posicao;
        this.enunciadoSnapshot = pergunta.getEnunciado();
        this.gabaritoSnapshot = pergunta.getGabarito();
        this.topicoNomeSnapshot = pergunta.getTopico().getNome();
        this.topicoIdSnapshot = pergunta.getTopico().getId();
        this.disciplinaNomeSnapshot = pergunta.getTopico().getDisciplina().getNome();
    }

    public ItemSessao(
            SessaoEstudo sessao,
            Pergunta perguntaOriginal,
            int posicao,
            Long topicoId,
            String topicoNome,
            String disciplinaNome,
            String enunciado,
            String gabarito
    ) {
        this.sessao = sessao;
        this.perguntaOriginal = perguntaOriginal;
        this.posicao = posicao;
        this.topicoIdSnapshot = topicoId;
        this.topicoNomeSnapshot = topicoNome;
        this.disciplinaNomeSnapshot = disciplinaNome;
        this.enunciadoSnapshot = enunciado;
        this.gabaritoSnapshot = gabarito;
    }

    public void registrarResposta(String resposta, java.time.LocalDateTime respondidaEm) {
        if (this.respondidaEm != null) {
            throw new IllegalStateException("A resposta deste item já foi registrada.");
        }
        this.respostaEstudante = resposta == null ? "" : resposta.trim();
        this.respondidaEm = respondidaEm;
    }

    public void registrarAutoavaliacao(
            Autoavaliacao autoavaliacao,
            java.time.LocalDateTime autoavaliadaEm
    ) {
        if (respondidaEm == null) {
            throw new IllegalStateException("Registre a resposta antes da autoavaliação.");
        }
        if (this.autoavaliacao != null) {
            throw new IllegalStateException("Este item já foi autoavaliado.");
        }
        this.autoavaliacao = autoavaliacao;
        this.autoavaliadaEm = autoavaliadaEm;
    }

    public void reiniciarTentativa() {
        this.respostaEstudante = null;
        this.autoavaliacao = null;
        this.respondidaEm = null;
        this.autoavaliadaEm = null;
    }
}