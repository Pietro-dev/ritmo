package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    // TODO: Evitar carregar toda a tabela em memória
    // TODO: Avaliar paginação para produção
    
    /*
    * Consulta personalizada que permite filtrar:
    * todos os registros
    * registros de um unico usuario (levando o e-mail em consideração)
    * registro a partir e até determinada data
    * além disso, a consulta permite combinar estes filtros entre si
    * por padrão, a consulta ordena os dados de forma descrescente (com base na data)
    */
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
        ORDER BY data_hora DESC
        """, nativeQuery = true)
    List<Auditoria> pesquisar(
            @Param("email") String email,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
