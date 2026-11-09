package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Pergunta;
import app.ritmo.aplicacaoritmo.domain.Topico;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.PerguntaInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerguntaService {

    private final PerguntaRepository perguntaRepository;
    private final TopicoRepository topicoRepository;
    private final SecurityUtils securityUtils;
    private final AuditoriaService auditoriaService;

    /*
    * crud de perguntas de acordo com o padrão
    * busca id do usuário logado,
    * reliza operação,
    * escreve ação na tabela de auditorias
    */

    @Transactional
    public void cadastrar(Long disciplinaId, Long topicoId, PerguntaInputDTO dto) {
        Usuario usuario = usuarioLogado();
        Topico topico = buscarTopicoDoUsuario(disciplinaId, topicoId, usuario.getId());

        Pergunta pergunta = new Pergunta(normalizarTexto(dto.enunciado()), normalizarTexto(dto.gabarito()), topico);
        perguntaRepository.save(pergunta);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.CRIAR_PERGUNTA, "Criou uma pergunta no tópico: " + topico.getNome());
    }

    @Transactional
    public List<PerguntaOutputDTO> listarPerguntasDoTopico(Long disciplinaId, Long topicoId) {
        Usuario usuario = usuarioLogado();
        buscarTopicoDoUsuario(disciplinaId, topicoId, usuario.getId());

        return perguntaRepository.findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(topicoId, disciplinaId,usuario.getId())
                .stream()
                .map(this::paraOutput)
                .toList();
    }

    @Transactional
    public PerguntaInputDTO buscarParaEdicao(Long disciplinaId, Long topicoId, Long perguntaId) {
        Usuario usuario = usuarioLogado();
        Pergunta pergunta = buscarPerguntaDoUsuario(disciplinaId, topicoId, perguntaId, usuario.getId());

        return new PerguntaInputDTO(pergunta.getEnunciado(), pergunta.getGabarito());
    }

    @Transactional
    public void atualizar(Long disciplinaId, Long topicoId, Long perguntaId, PerguntaInputDTO dto) {
        Usuario usuario = usuarioLogado();
        Pergunta pergunta = buscarPerguntaDoUsuario(disciplinaId, topicoId, perguntaId, usuario.getId());

        pergunta.setEnunciado(normalizarTexto(dto.enunciado()));
        pergunta.setGabarito(normalizarTexto(dto.gabarito()));
        perguntaRepository.save(pergunta);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.ATUALIZAR_PERGUNTA, "Atualizou uma pergunta no tópico: " + pergunta.getTopico().getNome());
    }

    @Transactional
    public void excluir(Long disciplinaId, Long topicoId, Long perguntaId) {
        Usuario usuario = usuarioLogado();
        Pergunta pergunta = buscarPerguntaDoUsuario(disciplinaId, topicoId, perguntaId, usuario.getId());
        String nomeTopico = pergunta.getTopico().getNome();

        perguntaRepository.delete(pergunta);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.EXCLUIR_PERGUNTA, "Excluiu uma pergunta do tópico: " + nomeTopico
        );
    }

    /*
    * MÉTODOS AUXILIARES
    */

    private Usuario usuarioLogado(){
        return securityUtils.getUsuarioLogado();
    }


    private Topico buscarTopicoDoUsuario(Long disciplinaId, Long topicoId, Long usuarioId) {
        return topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(topicoId,disciplinaId, usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontrado("Tópico não encontrado")
                );
    }

    private Pergunta buscarPerguntaDoUsuario(Long disciplinaId, Long topicoId, Long perguntaId, Long usuarioId) {
        return perguntaRepository.findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(perguntaId, topicoId, disciplinaId, usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontrado("Pergunta não encontrada")
                );
    }

    private String normalizarTexto(String texto) {
        return texto.trim();
    }

    private PerguntaOutputDTO paraOutput(Pergunta pergunta) {
        return new PerguntaOutputDTO(pergunta.getId(), pergunta.getEnunciado(), pergunta.getGabarito());
    }
}
