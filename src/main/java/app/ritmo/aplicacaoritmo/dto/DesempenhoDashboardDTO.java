package app.ritmo.aplicacaoritmo.dto;

public record DesempenhoDashboardDTO(
        String categoria,
        String disciplina,
        long total,
        long faceis,
        long dificeis,
        long naoLembrei
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
        return total == 0 ? 0 : (int) Math.round(quantidade * 100.0 / total);
    }
}
