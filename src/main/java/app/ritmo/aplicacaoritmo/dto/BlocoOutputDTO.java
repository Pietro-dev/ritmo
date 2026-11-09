package app.ritmo.aplicacaoritmo.dto;

import java.util.List;

public record BlocoOutputDTO(
        int duracaoMinutos,
        int quantidadePerguntas,
        List<TopicoConfiguradoDTO> topicos
) {
    public record TopicoConfiguradoDTO(
            Long id,
            String nome,
            long perguntasDisponiveis,
            int perguntasNoBloco
    ) {
    }
}