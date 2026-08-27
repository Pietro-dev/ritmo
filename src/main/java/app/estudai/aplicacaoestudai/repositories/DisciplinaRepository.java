package app.estudai.aplicacaoestudai.repositories;

import app.estudai.aplicacaoestudai.domain.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
}
