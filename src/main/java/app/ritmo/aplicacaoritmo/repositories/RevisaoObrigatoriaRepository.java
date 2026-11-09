package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.RevisaoObrigatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RevisaoObrigatoriaRepository
        extends JpaRepository<RevisaoObrigatoria, Long> {

    List<RevisaoObrigatoria>
    findByUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNullOrderByAgendadaParaAsc(
            Long usuarioId
    );

    List<RevisaoObrigatoria> findByUsuario_IdAndConcluidaEmIsNullOrderByAgendadaParaAsc(
            Long usuarioId
    );

    List<RevisaoObrigatoria> findByIdInAndUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNull(
            Collection<Long> ids,
            Long usuarioId
    );

    List<RevisaoObrigatoria> findBySessaoRevisao_IdAndConcluidaEmIsNull(Long sessaoId);

    long countByUsuario_IdAndConcluidaEmIsNull(Long usuarioId);

    long countByUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNullAndAgendadaParaLessThanEqual(
            Long usuarioId,
            LocalDate data
    );

    long countByConcluidaEmIsNull();

    long countByConcluidaEmIsNotNull();

    @Query(value = """
            SELECT COUNT(*)
            FROM revisao_obrigatoria
            WHERE concluida_em >= CAST(:inicio AS DATE)
              AND concluida_em < CAST(:fim AS DATE)
            """, nativeQuery = true)
    long countConcluidasNoPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
