CREATE TABLE topico (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    disciplina_id BIGINT NOT NULL,
    CONSTRAINT fk_topico_disciplina
        FOREIGN KEY (disciplina_id)
        REFERENCES disciplina(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_topico_disciplina_nome
    ON topico (disciplina_id, LOWER(nome));

CREATE TABLE pergunta (
    id BIGSERIAL PRIMARY KEY,
    enunciado TEXT NOT NULL,
    gabarito TEXT NOT NULL,
    topico_id BIGINT NOT NULL,
    CONSTRAINT fk_pergunta_topico
        FOREIGN KEY (topico_id)
        REFERENCES topico(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_pergunta_topico_id
    ON pergunta (topico_id);