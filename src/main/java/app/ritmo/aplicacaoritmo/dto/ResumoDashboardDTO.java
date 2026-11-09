package app.ritmo.aplicacaoritmo.dto;

import java.util.List;

public record ResumoDashboardDTO(
        long sessoesConcluidas,
        long minutosEstudados,
        long perguntasAvaliadas,
        long faceis,
        long dificeis,
        long naoLembrei,
        long revisoesPendentes,
        long revisoesDisponiveis,
        List<DesempenhoDashboardDTO> porDisciplina,
        List<DesempenhoDashboardDTO> porTopico
) {
    public int percentualFacil() {
        return percentual(faceis);
    }

    public int percentualDificil() {
        return percentual(dificeis);
    }

    public int percentualNaoLembrei() {
        return percentual(naoLembrei);
    }

    private int percentual(long quantidade) {
        return perguntasAvaliadas == 0
                ? 0
                : (int) Math.round(quantidade * 100.0 / perguntasAvaliadas);
    }
}
