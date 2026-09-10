package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.dto.DisciplinaInputDTO;
import app.ritmo.aplicacaoritmo.dto.DisciplinaOutputDTO;
import app.ritmo.aplicacaoritmo.exceptions.EntidadeDuplicadaException;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.exceptions.RecursoNaoEncontrado;
import app.ritmo.aplicacaoritmo.repositories.DisciplinaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DisciplinaService {
    @Autowired
    private DisciplinaRepository repository;

    @Transactional
    public void cadastrar(DisciplinaInputDTO dto) {
        String nomePadronizado = dto.nome().trim().toLowerCase();
        // verfica se a disciplina existe
        if(repository.existsByNome(nomePadronizado)){
            throw new EntidadeDuplicadaException("Já existe uma disciplina cadastrada com esse nome");
        }
        // padroniza o salvamento de disciplinas no banco com letra minúscula
        Disciplina disciplina = new Disciplina(nomePadronizado);
        repository.save(disciplina);
    }

    public List<DisciplinaOutputDTO> listar(){
        // na listagem de disciplinas, para melhor user experience, capitalizamos as disciplinas
        return repository.findAll().stream()
                .map(d -> new DisciplinaOutputDTO(d.getId(), capitalizar(d.getNome())))
                .collect(Collectors.toList());
    }

    // metodo para mostrar disciplinas com a primeira letra em maíusculo. Melhorando a UX
    public static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        // Retorna a primeira letra em maiúscula + o restante da string
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    // metodo para buscar a displina pelo id (e permitir posterior edição)
    public DisciplinaOutputDTO buscarPeloId(Long id) {
        Disciplina disciplina = repository.findById(id).orElseThrow(()-> new RecursoNaoEncontrado("Disciplina não encontrada!"));

        // se a disciplina for encontrada pelo id, cria e retorna um novo dto
        return new DisciplinaOutputDTO(disciplina.getId(), capitalizar(disciplina.getNome()));
    }

    // metodo para atualizar uma disciplina
    @Transactional
    public void atualizar(Long id, DisciplinaInputDTO dto) {
        Disciplina disciplina = repository.findById(id).orElseThrow(()->new RecursoNaoEncontrado("Disciplina não encontrada!"));

        String novoNomePadronizado = dto.nome().trim().toLowerCase();

        // não é possível renomear uma disciplina com o nome de outra disciplina
        if (repository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new EntidadeDuplicadaException("Já existe uma disciplina com esse nome!");
        }

        disciplina.setNome(dto.nome());
        repository.save(disciplina);
    }

    // exclusao de disciplinas
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontrado("Não é possível excluir uma disciplina inexistente!");
        }
        repository.deleteById(id);
    }
}
