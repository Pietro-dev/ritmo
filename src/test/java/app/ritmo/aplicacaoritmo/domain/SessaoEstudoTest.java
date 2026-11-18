package app.ritmo.aplicacaoritmo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessaoEstudoTest {

    @Test
    void percorreEstadosPendentesEmAndamentoEConcluida() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        SessaoEstudo sessao = new SessaoEstudo(usuario, 15);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 9, 10, 0);
        LocalDateTime fim = inicio.plusMinutes(15);

        assertEquals(StatusSessao.PENDENTE, sessao.getStatus());
        assertNull(sessao.getInicioEm());
        assertNull(sessao.getFimEm());

        sessao.iniciarEm(inicio);
        assertEquals(StatusSessao.EM_ANDAMENTO, sessao.getStatus());
        assertEquals(inicio, sessao.getInicioEm());
        assertNull(sessao.getFimEm());

        sessao.concluirEm(fim);
        assertEquals(StatusSessao.CONCLUIDA, sessao.getStatus());
        assertEquals(fim, sessao.getFimEm());
    }

    @Test
    void naoPermiteConcluirSessaoQueAindaNaoFoiIniciada() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        SessaoEstudo sessao = new SessaoEstudo(usuario, 5);

        assertThrows(
                IllegalStateException.class,
                () -> sessao.concluirEm(LocalDateTime.now())
        );
    }

    @Test
    void naoPermiteDataDeConclusaoAnteriorAoInicio() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        SessaoEstudo sessao = new SessaoEstudo(usuario, 5);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 9, 10, 0);
        sessao.iniciarEm(inicio);

        assertThrows(
                IllegalArgumentException.class,
                () -> sessao.concluirEm(inicio.minusSeconds(1))
        );
    }

    @Test
    void interromperDevolveSessaoAoEstadoPendenteEPermiteReinicio() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        SessaoEstudo sessao = new SessaoEstudo(usuario, 5);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 9, 10, 0);

        sessao.iniciarEm(inicio);
        sessao.interromper();

        assertEquals(StatusSessao.PENDENTE, sessao.getStatus());
        assertNull(sessao.getInicioEm());
        assertNull(sessao.getFimEm());
        sessao.iniciarEm(inicio.plusMinutes(1));
        assertEquals(StatusSessao.EM_ANDAMENTO, sessao.getStatus());
    }

    @Test
    void prazoFinalCorrespondeADuracaoDoBloco() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        SessaoEstudo sessao = new SessaoEstudo(usuario, 15);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 9, 10, 0);
        sessao.iniciarEm(inicio);

        assertEquals(inicio.plusMinutes(15), sessao.prazoFinal());
    }
}
