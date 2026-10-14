package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Topico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicoRepository extends JpaRepository<Topico, Long> {
    Optional<Topico> findByIdAndDisciplina_IdAndDisciplina_Usuario_Id(Long disciplinaId, Long topicoId, Long usuarioId);

    List<Topico> findByDisciplina_IdAndDisciplina_Usuario_IdOrderByNomeAsc(Long disciplinaId, Long usuarioId);

    Optional<Topico> findByIdAndDisciplina_Usuario_Id(Long topicoId, Long usuarioId);

    boolean existsByDisciplina_IdAndNomeIgnoreCase(Long disciplinaId, String nome);

    boolean existsByDisciplina_IdAndNomeIgnoreCaseAndIdNot(Long disciplinaId, String nome, Long topicoId);
}
