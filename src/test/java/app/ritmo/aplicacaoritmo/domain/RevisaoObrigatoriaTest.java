package app.ritmo.aplicacaoritmo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevisaoObrigatoriaTest {

    @Test
    void associaRevisaoMesmoQuandoSessaoDeOrigemAindaEstaPendente() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDate.now().atStartOfDay()
        );
        SessaoEstudo sessaoOrigem = new SessaoEstudo(usuario, 5);
        SessaoEstudo sessaoRevisao = new SessaoEstudo(usuario, 5, TipoSessao.REVISAO);
        RevisaoObrigatoria revisao = new RevisaoObrigatoria(
                usuario,
                sessaoOrigem,
                1L,
                "Equações",
                "Matemática",
                EtapaRevisao.HORAS_24,
                LocalDate.now()
        );

        revisao.associarASessao(sessaoRevisao);

        assertEquals(StatusSessao.PENDENTE, sessaoOrigem.getStatus());
        assertEquals(sessaoRevisao, revisao.getSessaoRevisao());
    }

    @Test
    void naoPermiteAssociarRevisaoUmaSegundaVez() {
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "senha",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDate.now().atStartOfDay()
        );
        RevisaoObrigatoria revisao = new RevisaoObrigatoria(
                usuario,
                new SessaoEstudo(usuario, 5),
                1L,
                "Equações",
                "Matemática",
                EtapaRevisao.HORAS_24,
                LocalDate.now()
        );
        revisao.associarASessao(new SessaoEstudo(usuario, 5, TipoSessao.REVISAO));

        assertNotNull(revisao.getSessaoRevisao());
        assertThrows(
                IllegalStateException.class,
                () -> revisao.associarASessao(new SessaoEstudo(usuario, 5, TipoSessao.REVISAO))
        );
    }
}
