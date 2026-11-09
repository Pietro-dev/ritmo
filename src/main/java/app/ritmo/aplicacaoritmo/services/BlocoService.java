package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.BlocoOutputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.dto.TopicoOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BlocoService {

    // definindo durações dos blocos de estudo
    private static final Set<Integer> DURACOES_PERMITIDAS = Set.of(5, 15, 30);

    private final TopicoService topicoService;

    public BlocoOutputDTO configurar(BlocoInputDTO dto) {
        validarDuracao(dto.duracaoMinutos());

        // cada bloco de estudo precisa de pelo menos um tópico
        List<Long> topicoIds = dto.topicoIds();
        if (topicoIds == null || topicoIds.isEmpty()) {
            throw new NegocioException("Selecione pelo menos um tópico.");
        }

        // em cada tópico pode-se ter 1 disciplina para cada 5 minutos do tópico.
        int limiteTopicos = dto.duracaoMinutos() / 5;

        if (topicoIds.size() > limiteTopicos) {
            throw new NegocioException(
                    "Para um bloco de " + dto.duracaoMinutos()
                            + " minutos, selecione no máximo " + limiteTopicos
                            + " tópico(s)."
            );
        }

        // proibindo a seleção de mais de um tópico em um mesmo bloco
        if (new HashSet<>(topicoIds).size() != topicoIds.size()) {
            throw new NegocioException("O mesmo tópico não pode ser selecionado mais de uma vez.");
        }

        // valida que a disciplina pertence ao usuário autenticado.
        List<TopicoOutputDTO> topicosDaDisciplina = topicoService.listarTopicosDaDisciplina(dto.disciplinaId());

        Map<Long, TopicoOutputDTO> topicosPorId = topicosDaDisciplina.stream()
                .collect(Collectors.toMap(TopicoOutputDTO::id, Function.identity()));

        List<TopicoOutputDTO> topicosSelecionados = topicoIds.stream()
                .map(topicosPorId::get)
                .toList();

        if (topicosSelecionados.stream().anyMatch(topico -> topico == null)) {
            throw new NegocioException(
                    "Selecione apenas tópicos que pertençam à disciplina escolhida."
            );
        }

        int minimoPorTopico = (dto.duracaoMinutos() + topicoIds.size() - 1) / topicoIds.size();

        List<TopicoOutputDTO> topicosInsuficientes = topicosSelecionados.stream()
                .filter(topico -> topico.quantidadePerguntas() < minimoPorTopico)
                .toList();

        if (!topicosInsuficientes.isEmpty()) {
            String detalhes = topicosInsuficientes.stream()
                    .map(topico -> topico.nome() + " (" + topico.quantidadePerguntas()
                            + " " + textoPerguntas(topico.quantidadePerguntas())
                            + " disponíveis)")
                    .collect(Collectors.joining(", "));

            throw new NegocioException(
                    "Para um bloco de " + dto.duracaoMinutos() + " minutos com "
                            + topicoIds.size() + " " + textoTopicos(topicoIds.size())
                            + ", cada tópico precisa ter pelo menos "
                            + minimoPorTopico + " perguntas disponíveis. "
                            + "Quantidade insuficiente: " + detalhes + "."
            );
        }

        int totalPerguntas = dto.duracaoMinutos();
        int quantidadeBase = totalPerguntas / topicoIds.size();
        int restante = totalPerguntas % topicoIds.size();

        List<BlocoOutputDTO.TopicoConfiguradoDTO> topicosConfigurados =
                IntStream.range(0, topicosSelecionados.size())
                        .mapToObj(indice -> {
                            TopicoOutputDTO topico = topicosSelecionados.get(indice);
                            int perguntasNoBloco = quantidadeBase + (indice < restante ? 1 : 0);

                            return new BlocoOutputDTO.TopicoConfiguradoDTO(
                                    topico.id(),
                                    topico.nome(),
                                    topico.quantidadePerguntas(),
                                    perguntasNoBloco
                            );
                        })
                        .toList();

        return new BlocoOutputDTO(
                dto.duracaoMinutos(),
                totalPerguntas,
                topicosConfigurados
        );
    }

    private void validarDuracao(Integer duracaoMinutos) {
        if (duracaoMinutos == null || !DURACOES_PERMITIDAS.contains(duracaoMinutos)) {
            throw new NegocioException(
                    "Escolha uma duração de 5, 15 ou 30 minutos para o bloco."
            );
        }
    }

    private String textoPerguntas(long quantidade) {
        return quantidade == 1 ? "pergunta" : "perguntas";
    }

    private String textoTopicos(int quantidade) {
        return quantidade == 1 ? "tópico" : "tópicos";
    }
}