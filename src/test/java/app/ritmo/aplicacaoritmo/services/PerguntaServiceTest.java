package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.PerguntaInputDTO;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PerguntaServiceTest {

    @Test
    void bloqueiaAcessoEAlteracoesDePerguntasDuranteSessaoAtiva() {
        Long usuarioId = 42L;
        var perguntaRepository = mock(PerguntaRepository.class);
        var topicoRepository = mock(TopicoRepository.class);
        var sessaoRepository = mock(SessaoEstudoRepository.class);
        var securityUtils = mock(SecurityUtils.class);
        var auditoriaService = mock(AuditoriaService.class);
        Usuario usuario = new Usuario(
                "Estudante",
                "estudante@example.com",
                "hash",
                Role.ROLE_ESTUDANTE,
                StatusCadastro.ATIVO,
                LocalDateTime.now()
        );
        usuario.setId(usuarioId);
        when(securityUtils.getUsuarioLogado()).thenReturn(usuario);
        when(sessaoRepository.existsByUsuario_IdAndStatus(usuarioId, StatusSessao.EM_ANDAMENTO))
                .thenReturn(true);

        PerguntaService service = new PerguntaService(
                perguntaRepository,
                topicoRepository,
                sessaoRepository,
                securityUtils,
                auditoriaService
        );

        assertThrows(AccessDeniedException.class, () -> service.listarPerguntasDoTopico(1L, 2L));
        assertThrows(AccessDeniedException.class, () -> service.buscarParaEdicao(1L, 2L, 3L));
        assertThrows(AccessDeniedException.class, () ->
                service.cadastrar(1L, 2L, new PerguntaInputDTO("Pergunta", "Gabarito")));
        assertThrows(AccessDeniedException.class, () ->
                service.atualizar(1L, 2L, 3L, new PerguntaInputDTO("Pergunta", "Gabarito")));
        assertThrows(AccessDeniedException.class, () -> service.excluir(1L, 2L, 3L));

        verify(sessaoRepository, org.mockito.Mockito.times(5))
                .existsByUsuario_IdAndStatus(usuarioId, StatusSessao.EM_ANDAMENTO);
        verifyNoInteractions(perguntaRepository, topicoRepository, auditoriaService);
    }
}
