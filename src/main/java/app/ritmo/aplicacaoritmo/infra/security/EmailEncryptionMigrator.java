package app.ritmo.aplicacaoritmo.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Order(0)
@RequiredArgsConstructor
public class EmailEncryptionMigrator implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final EmailEncryptionConverter converter = new EmailEncryptionConverter();

    @Override
    public void run(ApplicationArguments args) {
        migrarUsuarioEmails();
        migrarAuditoriaEmails();
    }

    private void migrarUsuarioEmails() {
        List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(
                "SELECT id, email, email_hash FROM usuario"
        );
        for (Map<String, Object> usuario : usuarios) {
            Object rawEmail = usuario.get("email");
            Object rawHash = usuario.get("email_hash");
            if (rawEmail == null) {
                continue;
            }

            String email = rawEmail.toString();
            String encrypted = email.startsWith("enc:v1:")
                    ? email
                    : converter.convertToDatabaseColumn(email);
            String plaintextHashSource = email.startsWith("enc:v1:")
                    ? converter.convertToEntityAttribute(email)
                    : email;
            String hash = rawHash == null
                    ? EmailHash.of(plaintextHashSource)
                    : rawHash.toString();

            jdbcTemplate.update(
                    "UPDATE usuario SET email = ?, email_hash = ? WHERE id = ?",
                    encrypted,
                    hash,
                    usuario.get("id")
            );
        }
    }

    private void migrarAuditoriaEmails() {
        List<Map<String, Object>> registros = jdbcTemplate.queryForList(
                "SELECT id, usuario_email, usuario_email_hash FROM auditorias"
        );
        for (Map<String, Object> registro : registros) {
            Object rawEmail = registro.get("usuario_email");
            Object rawHash = registro.get("usuario_email_hash");
            if (rawEmail == null) {
                continue;
            }

            String email = rawEmail.toString();
            String encrypted = email.startsWith("enc:v1:")
                    ? email
                    : converter.convertToDatabaseColumn(email);
            String plaintextHashSource = email.startsWith("enc:v1:")
                    ? converter.convertToEntityAttribute(email)
                    : email;
            String hash = rawHash == null
                    ? EmailHash.of(plaintextHashSource)
                    : rawHash.toString();

            jdbcTemplate.update(
                    "UPDATE auditorias SET usuario_email = ?, usuario_email_hash = ? WHERE id = ?",
                    encrypted,
                    hash,
                    registro.get("id")
            );
        }
    }
}
