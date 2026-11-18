package app.ritmo.aplicacaoritmo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemSessaoTest {

    @Test
    void criaSnapshotDaPerguntaETopico() {
        Usuario usuario = usuario();
        Disciplina disciplina = new Disciplina("Matemática");
        Topico topico = new Topico("Álgebra", disciplina);
        topico.setId(7L);
        Pergunta pergunta = new Pergunta("Enunciado", "Resposta", topico);
        SessaoEstudo sessao = new SessaoEstudo(usuario, 5);

        ItemSessao item = new ItemSessao(sessao, pergunta, 1);

        assertEquals(sessao, item.getSessao());
        assertEquals("Enunciado", item.getEnunciadoSnapshot());
        assertEquals("Resposta", item.getGabaritoSnapshot());
        assertEquals(7L, item.getTopicoIdSnapshot());
        assertEquals("Álgebra", item.getTopicoNomeSnapshot());
        assertEquals("Matemática", item.getDisciplinaNomeSnapshot());
    }

    @Test
    void registraRespostaEExigeRespostaAntesDaAutoavaliacao() {
        ItemSessao item = item();
        LocalDateTime instante = LocalDateTime.of(2026, 11, 17, 12, 0);

        assertThrows(
                IllegalStateException.class,
                () -> item.registrarAutoavaliacao(Autoavaliacao.FACIL, instante)
        );

        item.registrarResposta("  minha resposta  ", instante);
        assertEquals("minha resposta", item.getRespostaEstudante());
        assertEquals(instante, item.getRespondidaEm());
        item.registrarAutoavaliacao(Autoavaliacao.DIFICIL, instante.plusMinutes(1));
        assertEquals(Autoavaliacao.DIFICIL, item.getAutoavaliacao());
        assertEquals(instante.plusMinutes(1), item.getAutoavaliadaEm());
    }

    @Test
    void rejeitaRespostasDuplicadasEAvaliacaoDuplicada() {
        ItemSessao item = item();
        LocalDateTime instante = LocalDateTime.of(2026, 11, 17, 12, 0);
        item.registrarResposta("resposta", instante);

        assertThrows(
                IllegalStateException.class,
                () -> item.registrarResposta("outra resposta", instante.plusMinutes(1))
        );
        item.registrarAutoavaliacao(Autoavaliacao.FACIL, instante.plusMinutes(1));
        assertThrows(
                IllegalStateException.class,
                () -> item.registrarAutoavaliacao(Autoavaliacao.FACIL, instante.plusMinutes(2))
        );
    }

    @Test
    void aceitaRespostaNulaComoVaziaEReiniciaTentativa() {
        ItemSessao item = item();
        LocalDateTime instante = LocalDateTime.of(2026, 11, 17, 12, 0);
        item.registrarResposta(null, instante);
        item.registrarAutoavaliacao(Autoavaliacao.NAO_LEMBRO, instante.plusMinutes(1));

        assertEquals("", item.getRespostaEstudante());
        item.reiniciarTentativa();

        assertNull(item.getRespostaEstudante());
        assertNull(item.getRespondidaEm());
        assertNull(item.getAutoavaliacao());
        assertNull(item.getAutoavaliadaEm());
    }

    private ItemSessao item() {
        return new ItemSessao(
                new SessaoEstudo(usuario(), 5),
                null,
                1,
                7L,
                "Álgebra",
                "Matemática",
                "Enunciado",
                "Gabarito"
        );
    }

    private Usuario usuario() {
        return new Usuario(
                "Estudante",
                "estudante@example.com",
                "hash",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.of(2026, 11, 17, 10, 0)
        );
    }
}
