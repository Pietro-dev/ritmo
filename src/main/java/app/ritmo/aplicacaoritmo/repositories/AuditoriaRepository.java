package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}
