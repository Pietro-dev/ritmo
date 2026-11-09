package app.ritmo.aplicacaoritmo.dto;

import app.ritmo.aplicacaoritmo.domain.Autoavaliacao;

public record PerguntaSessaoDTO(
        Long sessaoId,
        int posicaoAtual,
        int totalPerguntas,
        int duracaoMinutos,
        long segundosRestantes,
        String enunciado,
        String respostaEstudante,
        String gabarito,
        Autoavaliacao autoavaliacao
) {
    public boolean respostaEnviada() {
        return respostaEstudante != null;
    }

    public boolean avaliacaoEnviada() {
        return autoavaliacao != null;
    }
}