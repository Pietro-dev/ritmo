package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Disciplina;
import app.ritmo.aplicacaoritmo.dto.DisciplinaOutputDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
    boolean existsByNomeAndUsuarioId(String nome, Long id);
    boolean existsByNomeAndUsuarioIdAndIdNot(String nome, Long id, Long id2);
    List<Disciplina> findByUsuarioId(Long id);
    Optional<Disciplina> findByIdAndUsuarioId(Long id, Long usuarioId);
}
