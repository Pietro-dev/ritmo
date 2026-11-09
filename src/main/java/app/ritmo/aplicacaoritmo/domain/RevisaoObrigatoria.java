package app.ritmo.aplicacaoritmo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "revisao_obrigatoria",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_revisao_origem_topico_etapa",
                columnNames = {"sessao_origem_id", "topico_id_snapshot", "etapa"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisaoObrigatoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_origem_id", nullable = false)
    private SessaoEstudo sessaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_revisao_id")
    private SessaoEstudo sessaoRevisao;

    @Column(name = "topico_id_snapshot", nullable = false)
    private Long topicoIdSnapshot;

    @Column(name = "topico_nome_snapshot", nullable = false, length = 50)
    private String topicoNomeSnapshot;

    @Column(name = "disciplina_nome_snapshot", nullable = false, length = 255)
    private String disciplinaNomeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaRevisao etapa;

    @Column(name = "agendada_para", nullable = false)
    private LocalDate agendadaPara;

    @Column(name = "concluida_em")
    private LocalDate concluidaEm;

    public RevisaoObrigatoria(
            Usuario usuario,
            SessaoEstudo sessaoOrigem,
            Long topicoId,
            String topicoNome,
            String disciplinaNome,
            EtapaRevisao etapa,
            LocalDate agendadaPara
    ) {
        this.usuario = usuario;
        this.sessaoOrigem = sessaoOrigem;
        this.topicoIdSnapshot = topicoId;
        this.topicoNomeSnapshot = topicoNome;
        this.disciplinaNomeSnapshot = disciplinaNome;
        this.etapa = etapa;
        this.agendadaPara = agendadaPara;
    }

    public void associarASessao(SessaoEstudo sessaoRevisao) {
        if (concluidaEm != null || sessaoRevisao != null) {
            throw new IllegalStateException("Esta revisão já foi associada a uma sessão.");
        }
        this.sessaoRevisao = sessaoRevisao;
    }

    public RevisaoObrigatoria concluir(LocalDate dataConclusao) {
        if (sessaoRevisao == null || concluidaEm != null) {
            throw new IllegalStateException("Esta revisão não pode ser concluída neste estado.");
        }
        this.concluidaEm = dataConclusao;

        EtapaRevisao proximaEtapa = etapa.proximaEtapa();
        if (proximaEtapa == null) {
            return null;
        }

        return new RevisaoObrigatoria(
                usuario,
                sessaoOrigem,
                topicoIdSnapshot,
                topicoNomeSnapshot,
                disciplinaNomeSnapshot,
                proximaEtapa,
                proximaEtapa.proximaData(dataConclusao)
        );
    }

    public void liberarSessao() {
        if (concluidaEm == null) {
            this.sessaoRevisao = null;
        }
    }
}
