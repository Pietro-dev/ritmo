ALTER TABLE usuario
    ADD COLUMN inativado_ate TIMESTAMP;

UPDATE usuario
SET inativado_ate = inativado_em + INTERVAL '3 days'
WHERE status = 'INATIVO' AND inativado_em IS NOT NULL;
