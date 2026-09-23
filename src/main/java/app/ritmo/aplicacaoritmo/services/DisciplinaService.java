package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.DisciplinaInputDTO;
import app.ritmo.aplicacaoritmo.dto.DisciplinaOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.DisciplinaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository repository;
    private final SecurityUtils securityUtils;
    private final AuditoriaService auditoriaService;

    @Transactional
    public void cadastrar(DisciplinaInputDTO dto) {
        Usuario usuario = usuarioLogado();
        String nome = normalizarNome(dto.nome());

        if (repository.existsByNomeAndUsuarioId(nome, usuario.getId())) {
            throw new EntidadeDuplicadaException(
                    "Já existe uma disciplina cadastrada com esse nome"
            );
        }

        Disciplina disciplina = new Disciplina(nome);
        disciplina.setUsuario(usuario);

        repository.save(disciplina);

        auditoriaService.registrar(usuario.getEmail(), AcaoAuditoria.CRIAR_DISCIPLINA, "Criou a disciplina: " + nome);
    }

    @Transactional
    public List<DisciplinaOutputDTO> listarDoUsuarioLogado() {
        Usuario usuario = usuarioLogado();

        return repository.findByUsuarioId(usuario.getId())
                .stream()
                .map(this::paraOutput)
                .toList();
    }

    @Transactional
    public DisciplinaOutputDTO buscarPeloIdDoUsuario(Long id) {
        Usuario usuario = usuarioLogado();

        Disciplina disciplina = buscarEntidadeDoUsuario(id, usuario.getId());

        return paraOutput(disciplina);
    }

    @Transactional
    public void atualizar(Long id, DisciplinaInputDTO dto) {
        Usuario usuario = usuarioLogado();
        Disciplina disciplina = buscarEntidadeDoUsuario(id, usuario.getId());
        String novoNome = normalizarNome(dto.nome());

        if (repository.existsByNomeAndUsuarioIdAndIdNot(novoNome, usuario.getId(), id)) {
            throw new EntidadeDuplicadaException(
                    "Já existe uma disciplina com esse nome"
            );
        }

        String nomeAnterior = disciplina.getNome();
        disciplina.setNome(novoNome);

        // Não é obrigatório chamar save: a entidade está gerenciada pela transação.
        repository.save(disciplina);

        auditoriaService.registrar(usuario.getEmail(),AcaoAuditoria.ATUALIZAR_DISCIPLINA,"Alterou a disciplina de " + nomeAnterior + " para " + novoNome);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = usuarioLogado();
        Disciplina disciplina = buscarEntidadeDoUsuario(id, usuario.getId());

        repository.delete(disciplina);

        auditoriaService.registrar(usuario.getEmail(),AcaoAuditoria.EXCLUIR_DISCIPLINA,"Excluiu a disciplina: " + disciplina.getNome()
        );
    }

    private Disciplina buscarEntidadeDoUsuario(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontrado("Disciplina não encontrada"));
    }

    private Usuario usuarioLogado() {
        return securityUtils.getUsuarioLogado();
    }

    private String normalizarNome(String nome) {
        return nome.trim().toLowerCase();
    }

    private DisciplinaOutputDTO paraOutput(Disciplina disciplina) {
        return new DisciplinaOutputDTO(disciplina.getId(),capitalizar(disciplina.getNome()));
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        return texto.substring(0, 1).toUpperCase()
                + texto.substring(1);
    }
}