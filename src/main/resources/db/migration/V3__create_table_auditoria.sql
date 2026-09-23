CREATE TABLE auditorias (
    id BIGSERIAL PRIMARY KEY,
    usuario_email VARCHAR(255) NOT NULL,
    acao VARCHAR(80) NOT NULL,
    descricao VARCHAR(1000) NOT NULL,
    data_hora TIMESTAMP NOT NULL
);