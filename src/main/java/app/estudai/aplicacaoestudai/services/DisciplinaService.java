package app.estudai.aplicacaoestudai.services;

import app.estudai.aplicacaoestudai.domain.Disciplina;
import app.estudai.aplicacaoestudai.dto.DisciplinaInputDTO;
import app.estudai.aplicacaoestudai.dto.DisciplinaOutputDTO;
import app.estudai.aplicacaoestudai.exceptions.NegocioException;
import app.estudai.aplicacaoestudai.repositories.DisciplinaRepository;
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
        // verfica se a disciplina existe
        if(repository.existsByNome(dto.nome())){
            throw new NegocioException("Já existe uma disciplina cadastrada com esse nome");
        }
        // padroniza o salvamento de disciplinas no banco com letra minúscula
        Disciplina disciplina = new Disciplina(dto.nome().toLowerCase());
        disciplina = repository.save(disciplina);
    }

    public List<DisciplinaOutputDTO> listar(){
        // na listagem de disciplinas, para melhor user experience, capitalizamos as disciplinas
        return StreamSupport.stream(repository.findAll().spliterator(), false)
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
        Disciplina disciplina = repository.findById(id).orElseThrow(()-> new NegocioException("Disciplina não encontrada!"));

        // se a disciplina for encontrada pelo id, cria e retorna um novo dto
        return new DisciplinaOutputDTO(disciplina.getId(), disciplina.getNome());
    }

    // metodo para atualizar um disciplina
    @Transactional
    public void atualizar(Long id, DisciplinaInputDTO dto) {
        Disciplina disciplina = repository.findById(id).orElseThrow(()->new NegocioException("Disciplina não encontrada!"));

        // não é possível renomear uma disciplina com o nome de outra disciplina
        if (repository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new NegocioException("Já existe uma disciplina com esse nome!");
        }

        disciplina.setNome(dto.nome());
        repository.save(disciplina);
    }

    // exclusao de disciplinas
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new NegocioException("Não é possível excluir uma disciplina inesistente!");
        }
        repository.deleteById(id);
    }
}
