package app.ritmo.aplicacaoritmo.dto;

public record AtividadeAdminDTO(
        String periodo,
        long cadastros,
        long sessoes,
        int percentualCadastros,
        int percentualSessoes
) {
}
