package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.domain.TipoSessao;

import java.time.LocalDateTime;
import java.util.List;

public record SessaoResumoDTO(
        Long id,
        TipoSessao tipo,
        int duracaoMinutos,
        StatusSessao status,
        LocalDateTime criadaEm,
        LocalDateTime inicioEm,
        LocalDateTime fimEm,
        int quantidadePerguntas,
        List<TopicoResumoDTO> topicos
) {
    public record TopicoResumoDTO(
            String nome,
            int quantidadePerguntas
    ) {
    }
}