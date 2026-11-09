package app.ritmo.aplicacaoritmo.repositories;

import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Usuario> findByRoleAndStatus(Role role, StatusCadastro status);

    long countByStatus(StatusCadastro status);

    long countByDataCadastroGreaterThanEqualAndDataCadastroLessThan(
            LocalDateTime inicio,
            LocalDateTime fim
    );
}
