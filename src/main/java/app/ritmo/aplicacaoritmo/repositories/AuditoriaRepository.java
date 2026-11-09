package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Auditoria;
import app.ritmo.aplicacaoritmo.repositories.projections.AuditoriaAdminProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    long deleteByUsuarioEmailIgnoreCase(String usuarioEmail);

    @Query(value = """
            SELECT a.data_hora AS "dataHora", a.acao AS "acao"
            FROM auditorias a
            JOIN usuario u ON u.email = a.usuario_email
            WHERE u.role = 'ROLE_ADMIN'
              AND a.acao IN (
                  'CRIAR_CONTA', 'ATUALIZAR_PERFIL', 'CONSULTAR_AUDITORIAS',
                  'CRIAR_DISCIPLINA', 'ATUALIZAR_DISCIPLINA', 'EXCLUIR_DISCIPLINA',
                  'CRIAR_TOPICO', 'ATUALIZAR_TOPICO', 'EXCLUIR_TOPICO',
                  'CRIAR_PERGUNTA', 'ATUALIZAR_PERGUNTA', 'EXCLUIR_PERGUNTA'
              )
            ORDER BY a.data_hora DESC, a.id DESC
            LIMIT 5
            """, nativeQuery = true)
    List<AuditoriaAdminProjection> buscarAcoesAdministrativasRecentes();

    @Query(value = """
        SELECT *
        FROM auditorias
        WHERE (
            CAST(:email AS VARCHAR) IS NULL
            OR LOWER(usuario_email) LIKE LOWER(CONCAT('%', CAST(:email AS VARCHAR), '%'))
        )
        AND (
            CAST(:inicio AS TIMESTAMP) IS NULL
            OR data_hora >= CAST(:inicio AS TIMESTAMP)
        )
        AND (
            CAST(:fim AS TIMESTAMP) IS NULL
            OR data_hora < CAST(:fim AS TIMESTAMP)
        )
        ORDER BY data_hora DESC, id DESC
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM auditorias
        WHERE (
            CAST(:email AS VARCHAR) IS NULL
            OR LOWER(usuario_email) LIKE LOWER(CONCAT('%', CAST(:email AS VARCHAR), '%'))
        )
        AND (
            CAST(:inicio AS TIMESTAMP) IS NULL
            OR data_hora >= CAST(:inicio AS TIMESTAMP)
        )
        AND (
            CAST(:fim AS TIMESTAMP) IS NULL
            OR data_hora < CAST(:fim AS TIMESTAMP)
        )
        """,
        nativeQuery = true)
    Page<Auditoria> pesquisar(
            @Param("email") String email,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );
}
