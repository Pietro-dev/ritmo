CREATE TABLE disciplina (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_disciplina_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT uk_disciplina_usuario_nome UNIQUE (usuario_id, nome) );