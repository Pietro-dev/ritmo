package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.SessaoEstudo;
import app.ritmo.aplicacaoritmo.repositories.projections.DesempenhoProjection;
import app.ritmo.aplicacaoritmo.repositories.projections.ResumoSessaoProjection;
import app.ritmo.aplicacaoritmo.repositories.projections.AtividadeAdminProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface SessaoEstudoRepository extends JpaRepository<SessaoEstudo, Long> {

    Optional<SessaoEstudo> findByIdAndUsuario_Id(Long sessaoId,  Long usuarioId);

    @Query(value = """
            SELECT COUNT(*)
            FROM sessao_estudo
            WHERE status = 'CONCLUIDA'
              AND (CAST(:inicio AS TIMESTAMP) IS NULL OR fim_em >= CAST(:inicio AS TIMESTAMP))
              AND (CAST(:fim AS TIMESTAMP) IS NULL OR fim_em < CAST(:fim AS TIMESTAMP))
            """, nativeQuery = true)
    long contarSessoesConcluidasNaPlataforma(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM item_sessao item
            JOIN sessao_estudo sessao ON sessao.id = item.sessao_id
            WHERE sessao.status = 'CONCLUIDA'
              AND item.respondida_em IS NOT NULL
              AND (CAST(:inicio AS TIMESTAMP) IS NULL OR sessao.fim_em >= CAST(:inicio AS TIMESTAMP))
              AND (CAST(:fim AS TIMESTAMP) IS NULL OR sessao.fim_em < CAST(:fim AS TIMESTAMP))
            """, nativeQuery = true)
    long contarPerguntasRespondidasNaPlataforma(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            SELECT atividade.periodo AS "periodo",
                   SUM(atividade.cadastros) AS "cadastros",
                   SUM(atividade.sessoes) AS "sessoes"
            FROM (
                SELECT date_trunc(
                           CASE WHEN CAST(:inicio AS TIMESTAMP) IS NULL THEN 'month' ELSE 'day' END,
                           data_cadastro
                       ) AS periodo,
                       COUNT(*) AS cadastros,
                       0 AS sessoes
                FROM usuario
                WHERE CAST(:inicio AS TIMESTAMP) IS NULL
                   OR (data_cadastro >= CAST(:inicio AS TIMESTAMP)
                       AND data_cadastro < CAST(:fim AS TIMESTAMP))
                GROUP BY 1
                UNION ALL
                SELECT date_trunc(
                           CASE WHEN CAST(:inicio AS TIMESTAMP) IS NULL THEN 'month' ELSE 'day' END,
                           fim_em
                       ) AS periodo,
                       0 AS cadastros,
                       COUNT(*) AS sessoes
                FROM sessao_estudo
                WHERE status = 'CONCLUIDA'
                  AND (CAST(:inicio AS TIMESTAMP) IS NULL
                       OR (fim_em >= CAST(:inicio AS TIMESTAMP)
                           AND fim_em < CAST(:fim AS TIMESTAMP)))
                GROUP BY 1
            ) atividade
            GROUP BY atividade.periodo
            ORDER BY atividade.periodo
            """, nativeQuery = true)
    List<AtividadeAdminProjection> atividadeDaPlataforma(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            SELECT COUNT(*) AS "sessoesConcluidas",
                   COALESCE(SUM(duracao_minutos), 0) AS "minutosEstudados"
            FROM sessao_estudo
            WHERE usuario_id = :usuarioId
              AND status = 'CONCLUIDA'
              AND (CAST(:inicio AS TIMESTAMP) IS NULL OR fim_em >= CAST(:inicio AS TIMESTAMP))
              AND (CAST(:fim AS TIMESTAMP) IS NULL OR fim_em < CAST(:fim AS TIMESTAMP))
            """, nativeQuery = true)
    ResumoSessaoProjection resumirSessoesConcluidas(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            SELECT disciplina_nome_snapshot AS categoria,
                   disciplina_nome_snapshot AS disciplina,
                   COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE autoavaliacao = 'FACIL') AS "faceis",
                   COUNT(*) FILTER (WHERE autoavaliacao = 'DIFICIL') AS "dificeis",
                   COUNT(*) FILTER (WHERE autoavaliacao = 'NAO_LEMBRO') AS "naoLembrei"
            FROM item_sessao item
            JOIN sessao_estudo sessao ON sessao.id = item.sessao_id
            WHERE sessao.usuario_id = :usuarioId
              AND sessao.status = 'CONCLUIDA'
              AND (CAST(:inicio AS TIMESTAMP) IS NULL OR sessao.fim_em >= CAST(:inicio AS TIMESTAMP))
              AND (CAST(:fim AS TIMESTAMP) IS NULL OR sessao.fim_em < CAST(:fim AS TIMESTAMP))
              AND item.autoavaliacao IS NOT NULL
            GROUP BY disciplina_nome_snapshot
            ORDER BY disciplina_nome_snapshot
            """, nativeQuery = true)
    java.util.List<DesempenhoProjection> desempenhoPorDisciplina(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
            SELECT topico_nome_snapshot AS categoria,
                   disciplina_nome_snapshot AS disciplina,
                   COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE autoavaliacao = 'FACIL') AS "faceis",
                   COUNT(*) FILTER (WHERE autoavaliacao = 'DIFICIL') AS "dificeis",
                   COUNT(*) FILTER (WHERE autoavaliacao = 'NAO_LEMBRO') AS "naoLembrei"
            FROM item_sessao item
            JOIN sessao_estudo sessao ON sessao.id = item.sessao_id
            WHERE sessao.usuario_id = :usuarioId
              AND sessao.status = 'CONCLUIDA'
              AND (CAST(:inicio AS TIMESTAMP) IS NULL OR sessao.fim_em >= CAST(:inicio AS TIMESTAMP))
              AND (CAST(:fim AS TIMESTAMP) IS NULL OR sessao.fim_em < CAST(:fim AS TIMESTAMP))
              AND item.autoavaliacao IS NOT NULL
            GROUP BY disciplina_nome_snapshot, topico_nome_snapshot
            ORDER BY disciplina_nome_snapshot, topico_nome_snapshot
            """, nativeQuery = true)
    java.util.List<DesempenhoProjection> desempenhoPorTopico(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}