package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
    List<Pergunta> findByTopico_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(Long topicoId, Long usuarioId);

    Optional<Pergunta> findByIdAndTopico_IdAndTopico_Disciplina_Usuario_Id(Long perguntaId, Long topicoId, Long usuarioId);

    List<Pergunta> findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(Long topicoId, Long disciplinaId, Long usuarioId);

    Optional<Pergunta> findByIdAndTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_Id(Long perguntaId, Long topicoId, Long disciplinaId, Long usuarioId);

    long countByTopico_Id(Long topicoId);

    //para remover perguntas de um tópico
    void deleteByTopico_Id(Long topicoId);
}
