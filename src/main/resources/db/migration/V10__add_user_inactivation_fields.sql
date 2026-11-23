ALTER TABLE usuario
    ADD COLUMN motivo_inativacao VARCHAR(500),
    ADD COLUMN inativado_em TIMESTAMP;
