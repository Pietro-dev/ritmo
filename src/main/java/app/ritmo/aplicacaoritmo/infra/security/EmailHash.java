package app.ritmo.aplicacaoritmo.infra.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class EmailHash {
    private EmailHash() {
    }

    public static String of(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível gerar o índice do e-mail.", ex);
        }
    }
}
