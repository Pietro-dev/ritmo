package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.domain.Topico;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.TopicoInputDTO;
import app.ritmo.aplicacaoritmo.dto.TopicoOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.DisciplinaRepository;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicoService {

    //CRUD
    //REgistrar na tabela de auditoria
    // para qualquer ação do crud, preciso validade que o usuário está autenticado

    // Cadastro
    // receber o input DTO
    // pegar disciplina
    // verificar se não há um tópico com o mesmo nome cadastrado
    // prosseguir o cadastro definindo nome do tópico, atribuindo uma disciplina id a ele

    private final SecurityUtils securityUtils;
    private final DisciplinaRepository disciplinaRepository;
    private final TopicoRepository topicoRepository;
    private final AuditoriaService auditoriaService;
    private final PerguntaRepository perguntaRepository;

    /*
    * CRUD de tópicos
    * em todos as operações do crud, resgato o usuário logado para realizar as operações em cima do ID dele
    * o crud segue o padrão do crud de disciplinas, não permitindo o cadastro duplicado de tópicos com o mesmo nome
    * além de escrever as letrar em minúsculo no banco e capitalizar na exibição.
    */

    public void cadastrar (Long disciplinaId, TopicoInputDTO dto){
        Usuario usuario = usuarioLogado();
        Disciplina disciplina = buscarDisciplinaDoUsuario(disciplinaId, usuario.getId());
        String nome = normalizarNome(dto.nome());

        if(topicoRepository.existsByDisciplina_IdAndNomeIgnoreCase(disciplinaId, nome)){
            throw new EntidadeDuplicadaException("Já existe um tópico com este nome cadastrado nesta disciplina!");
        }

        Topico topico = new Topico(nome, disciplina);
        topicoRepository.save(topico);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.CRIAR_TOPICO, "Tópico criado na niscipina: " + disciplina.getNome());

    }

    @Transactional
    public List<TopicoOutputDTO> listarTopicosDaDisciplina (Long disciplinaId){
        Usuario usuario = usuarioLogado();
        buscarDisciplinaDoUsuario(disciplinaId, usuario.getId());

        return topicoRepository.findByDisciplina_IdAndDisciplina_Usuario_IdOrderByNomeAsc(disciplinaId, usuario.getId())
                .stream()
                .map(this::paraOutput)
                .toList();
    }

    @Transactional
    public TopicoOutputDTO buscarPeloId(Long disciplinaId, Long topicoId) {
        Usuario usuario = usuarioLogado();

        return paraOutput(buscarTopicoDoUsuario(disciplinaId, topicoId, usuario.getId()));
    }

    @Transactional
    public void atualizar(Long disciplinaId, Long topicoId, TopicoInputDTO dto) {
        Usuario usuario = usuarioLogado();
        Topico topico = buscarTopicoDoUsuario(disciplinaId, topicoId, usuario.getId());
        String novoNome = normalizarNome(dto.nome());

        if (topicoRepository.existsByDisciplina_IdAndNomeIgnoreCaseAndIdNot(disciplinaId, novoNome, topicoId
        )) {
            throw new EntidadeDuplicadaException("Já existe um tópico com esse nome nesta disciplina.");
        }

        String nomeAnterior = topico.getNome();
        topico.setNome(novoNome);
        topicoRepository.save(topico);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.ATUALIZAR_TOPICO, "Atualizou um tópico da disciplina: " + topico.getDisciplina().getNome() + " (nome anterior: " + nomeAnterior + ")"
        );
    }

    @Transactional
    public void excluir(Long disciplinaId, Long topicoId) {
        Usuario usuario = usuarioLogado();
        Topico topico = buscarTopicoDoUsuario(disciplinaId, topicoId, usuario.getId());
        String nomeDisciplina = topico.getDisciplina().getNome();

        topicoRepository.delete(topico);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.EXCLUIR_TOPICO, "Excluiu um tópico da disciplina: " + nomeDisciplina);
    }


    /*
    * MÉTODOS AUXILIARES
    */

    private Topico buscarTopicoDoUsuario(Long disciplinaId, Long topicoId, Long usuarioId
    ) {
        return topicoRepository.findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(topicoId, disciplinaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontrado("Tópico não encontrado"));
    }

    private String capitalizar(String nome) {
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    private Disciplina buscarDisciplinaDoUsuario(Long disciplinaId, Long usuarioId){
        return disciplinaRepository.findByIdAndUsuarioId(disciplinaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontrado("Disciplina não encontrada!"));
    }

    private Usuario usuarioLogado(){
        return securityUtils.getUsuarioLogado();
    }

    private String normalizarNome(String nome) {
        return nome.trim().toLowerCase();
    }

    private TopicoOutputDTO paraOutput(Topico topico){
        return new TopicoOutputDTO(topico.getId(), capitalizar(topico.getNome()), perguntaRepository.countByTopico_Id(topico.getId()));
    }
}
