ALTER TABLE usuario
    ALTER COLUMN email TYPE VARCHAR(1024),
    ADD COLUMN email_hash VARCHAR(64);

ALTER TABLE auditorias
    ALTER COLUMN usuario_email TYPE VARCHAR(1024),
    ADD COLUMN usuario_email_hash VARCHAR(64);

CREATE UNIQUE INDEX ux_usuario_email_hash
    ON usuario(email_hash)
    WHERE email_hash IS NOT NULL;
