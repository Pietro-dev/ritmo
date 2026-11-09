package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.ItemSessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemSessaoRepository extends JpaRepository<ItemSessao, Long> {

    List<ItemSessao> findBySessao_IdOrderByPosicaoAsc(Long sessaoId);

    Optional<ItemSessao> findFirstBySessao_IdAndSessao_Usuario_IdOrderByPosicaoAsc(Long sessaoId, Long usuarioId);

    List<ItemSessao> findBySessao_IdAndTopicoIdSnapshotOrderByPosicaoAsc(
            Long sessaoId,
            Long topicoIdSnapshot
    );

    List<ItemSessao> findBySessao_IdAndAutoavaliacaoIsNullOrderByPosicaoAsc(Long sessaoId);
}