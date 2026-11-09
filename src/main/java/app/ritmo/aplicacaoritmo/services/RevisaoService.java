package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.EtapaRevisao;
import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.ItemSessao;
import app.ritmo.aplicacaoritmo.domain.RevisaoObrigatoria;
import app.ritmo.aplicacaoritmo.domain.SessaoEstudo;
import app.ritmo.aplicacaoritmo.domain.TipoSessao;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.BlocoRevisaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.RevisaoDisponivelDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.ItemSessaoRepository;
import app.ritmo.aplicacaoritmo.repositories.RevisaoObrigatoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RevisaoService {

    private static final Set<Integer> DURACOES_PERMITIDAS = Set.of(5, 15, 30);

    private final SecurityUtils securityUtils;
    private final RevisaoObrigatoriaRepository revisaoRepository;
    private final SessaoEstudoRepository sessaoRepository;
    private final ItemSessaoRepository itemSessaoRepository;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    @Transactional
    public List<RevisaoDisponivelDTO> listarRevisoes() {
        Usuario usuario = securityUtils.getUsuarioLogado();
        LocalDate hoje = LocalDate.now(clock);

        return revisaoRepository
                .findByUsuario_IdAndConcluidaEmIsNullOrderByAgendadaParaAsc(
                        usuario.getId()
                )
                .stream()
                .map(revisao -> {
                    int quantidadePerguntas = itemSessaoRepository
                            .findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(
                                    revisao.getSessaoOrigem().getId(),
                                    revisao.getTopicoIdSnapshot()
                            )
                            .size();
                    return new RevisaoDisponivelDTO(
                            revisao.getId(),
                            revisao.getTopicoIdSnapshot(),
                            revisao.getTopicoNomeSnapshot(),
                            revisao.getDisciplinaNomeSnapshot(),
                            revisao.getEtapa(),
                            revisao.getAgendadaPara(),
                            quantidadePerguntas,
                            !revisao.getAgendadaPara().isAfter(hoje),
                            revisao.getSessaoRevisao() == null
                                    ? null : revisao.getSessaoRevisao().getId(),
                            revisao.getSessaoRevisao() == null
                                    ? null : revisao.getSessaoRevisao().getStatus()
                    );
                })
                .toList();
    }

    @Transactional
    public Long criarBloco(BlocoRevisaoInputDTO dto) {
        validarDuracao(dto.duracaoMinutos());
        List<Long> revisaoIds = dto.revisaoIds();

        if (revisaoIds == null || revisaoIds.isEmpty()) {
            throw new NegocioException("Selecione ao menos um tópico com revisão pendente.");
        }
        if (new HashSet<>(revisaoIds).size() != revisaoIds.size()) {
            throw new NegocioException("Não selecione a mesma revisão mais de uma vez.");
        }

        Usuario usuario = securityUtils.getUsuarioLogado();
        List<RevisaoObrigatoria> revisoes = revisaoRepository
                .findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
                        revisaoIds,
                        usuario.getId()
                );
        if (revisoes.size() != revisaoIds.size()) {
            throw new NegocioException(
                    "Uma ou mais revisões não estão disponíveis para esta conta."
            );
        }

        LocalDate hoje = LocalDate.now(clock);
        if (revisoes.stream().anyMatch(revisao -> revisao.getAgendadaPara().isAfter(hoje))) {
            throw new NegocioException("Este bloco inclui revisões que ainda não estão liberadas.");
        }

        Set<Long> topicoIds = revisoes.stream()
                .map(RevisaoObrigatoria::getTopicoIdSnapshot)
                .collect(Collectors.toSet());
        if (topicoIds.size() != revisoes.size()) {
            throw new NegocioException(
                    "Selecione apenas uma revisão pendente por tópico neste bloco."
            );
        }

        int limiteTopicos = dto.duracaoMinutos() / 5;
        if (revisoes.size() > limiteTopicos) {
            throw new NegocioException(
                    "Um bloco de revisão de " + dto.duracaoMinutos()
                            + " minutos permite até " + limiteTopicos + " tópicos."
            );
        }

        Map<Long, RevisaoObrigatoria> revisoesPorId = revisoes.stream()
                .collect(Collectors.toMap(RevisaoObrigatoria::getId, Function.identity()));
        List<RevisaoObrigatoria> selecionadas = revisaoIds.stream()
                .map(revisoesPorId::get)
                .toList();

        List<List<ItemSessao>> itensPorTopico = selecionadas.stream()
                .map(revisao -> itemSessaoRepository
                        .findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(
                                revisao.getSessaoOrigem().getId(),
                                revisao.getTopicoIdSnapshot()
                        ))
                .toList();

        int minimoPorTopico =
                (dto.duracaoMinutos() + selecionadas.size() - 1) / selecionadas.size();
        for (int indice = 0; indice < selecionadas.size(); indice++) {
            if (itensPorTopico.get(indice).size() < minimoPorTopico) {
                RevisaoObrigatoria revisao = selecionadas.get(indice);
                throw new NegocioException(
                        "O tópico \"" + revisao.getTopicoNomeSnapshot()
                                + "\" não tem perguntas suficientes para este bloco de revisão."
                );
            }
        }

        SessaoEstudo sessao = sessaoRepository.save(
                new SessaoEstudo(usuario, dto.duracaoMinutos(), TipoSessao.REVISAO)
        );
        int quantidadeBase = dto.duracaoMinutos() / selecionadas.size();
        int restante = dto.duracaoMinutos() % selecionadas.size();
        List<ItemSessao> itensDoBloco = new ArrayList<>();
        int posicao = 1;

        for (int indice = 0; indice < selecionadas.size(); indice++) {
            RevisaoObrigatoria revisao = selecionadas.get(indice);
            List<ItemSessao> itensOriginais = itensPorTopico.get(indice);
            int quantidadeNoBloco = quantidadeBase + (indice < restante ? 1 : 0);

            for (int itemIndice = 0; itemIndice < quantidadeNoBloco; itemIndice++) {
                ItemSessao original = itensOriginais.get(itemIndice);
                itensDoBloco.add(new ItemSessao(
                        sessao,
                        original.getPerguntaOriginal(),
                        posicao++,
                        revisao.getTopicoIdSnapshot(),
                        revisao.getTopicoNomeSnapshot(),
                        revisao.getDisciplinaNomeSnapshot(),
                        original.getEnunciadoSnapshot(),
                        original.getGabaritoSnapshot()
                ));
            }
            revisao.associarASessao(sessao);
        }

        itemSessaoRepository.saveAll(itensDoBloco);
        revisaoRepository.saveAll(selecionadas);
        auditoriaService.registrar(
                usuario.getEmail(),
                AcaoAuditoria.CRIAR_BLOCO_REVISAO,
                "Criou a sessão de revisão #" + sessao.getId() + " com duração de "
                        + dto.duracaoMinutos() + " minutos."
        );
        return sessao.getId();
    }

    @Transactional
    public void agendarPrimeirasRevisoes(SessaoEstudo sessao, LocalDate concluidaEm) {
        if (sessao.getTipo() != TipoSessao.ESTUDO) {
            return;
        }

        Map<Long, ItemSessao> topicosEstudados = itemSessaoRepository
                .findBySessao_IdOrderByPosicaoAsc(sessao.getId())
                .stream()
                .filter(item -> item.getTopicoIdSnapshot() != null)
                .collect(Collectors.toMap(
                        ItemSessao::getTopicoIdSnapshot,
                        Function.identity(),
                        (primeiro, ignorado) -> primeiro
                ));

        List<RevisaoObrigatoria> revisoes = topicosEstudados.values().stream()
                .map(item -> new RevisaoObrigatoria(
                        sessao.getUsuario(),
                        sessao,
                        item.getTopicoIdSnapshot(),
                        item.getTopicoNomeSnapshot(),
                        item.getDisciplinaNomeSnapshot(),
                        EtapaRevisao.HORAS_24,
                        EtapaRevisao.HORAS_24.proximaData(concluidaEm)
                ))
                .toList();
        revisaoRepository.saveAll(revisoes);
        if (!revisoes.isEmpty()) {
            auditoriaService.registrar(
                    sessao.getUsuario().getEmail(),
                    AcaoAuditoria.AGENDAR_REVISOES,
                    "Agendou " + revisoes.size() + " revisões para a sessão #"
                            + sessao.getId() + "."
            );
        }
    }

    @Transactional
    public void agendarProximasRevisoes(SessaoEstudo sessao, LocalDate concluidaEm) {
        if (sessao.getTipo() != TipoSessao.REVISAO) {
            return;
        }

        List<RevisaoObrigatoria> revisoesConcluidas = revisaoRepository
                .findBySessaoRevisao_IdAndConcluidaEmIsNull(sessao.getId());
        List<RevisaoObrigatoria> proximas = new ArrayList<>();

        for (RevisaoObrigatoria revisao : revisoesConcluidas) {
            RevisaoObrigatoria proxima = revisao.concluir(concluidaEm);
            if (proxima != null) {
                proximas.add(proxima);
            }
        }
        revisaoRepository.saveAll(proximas);
        if (!revisoesConcluidas.isEmpty()) {
            auditoriaService.registrar(
                    sessao.getUsuario().getEmail(),
                    AcaoAuditoria.CONCLUIR_REVISOES,
                    "Concluiu " + revisoesConcluidas.size() + " revisões na sessão #"
                            + sessao.getId() + "."
            );
        }
    }

    @Transactional
    public void liberarRevisoesDaSessao(SessaoEstudo sessao) {
        if (sessao.getTipo() != TipoSessao.REVISAO) {
            return;
        }

        revisaoRepository.findBySessaoRevisao_IdAndConcluidaEmIsNull(sessao.getId())
                .forEach(RevisaoObrigatoria::liberarSessao);
    }

    private void validarDuracao(Integer duracao) {
        if (duracao == null || !DURACOES_PERMITIDAS.contains(duracao)) {
            throw new NegocioException("A duração do bloco deve ser 5, 15 ou 30 minutos.");
        }
    }
}
