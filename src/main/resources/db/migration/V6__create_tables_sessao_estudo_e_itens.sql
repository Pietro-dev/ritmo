CREATE TABLE sessao_estudo (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inicio_em TIMESTAMP NULL,
    fim_em TIMESTAMP NULL,

    CONSTRAINT fk_sessao_estudo_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,

    CONSTRAINT ck_sessao_estudo_duracao CHECK (duracao_minutos IN (5, 15, 30)),

    CONSTRAINT ck_sessao_estudo_status CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA')),

    CONSTRAINT ck_sessao_estudo_datas
       CHECK (fim_em IS NULL OR (inicio_em IS NOT NULL AND fim_em >= inicio_em))
);

CREATE INDEX idx_sessao_estudo_usuario_status ON sessao_estudo (usuario_id, status);

CREATE TABLE item_sessao (
    id BIGSERIAL PRIMARY KEY,
    sessao_id BIGINT NOT NULL,
    pergunta_id BIGINT NULL,
    posicao INTEGER NOT NULL,
    enunciado_snapshot TEXT NOT NULL,
    gabarito_snapshot TEXT NOT NULL,
    topico_nome_snapshot VARCHAR(50) NOT NULL,

    CONSTRAINT fk_item_sessao_sessao FOREIGN KEY (sessao_id) REFERENCES sessao_estudo(id) ON DELETE CASCADE,

    CONSTRAINT fk_item_sessao_pergunta FOREIGN KEY (pergunta_id) REFERENCES pergunta(id) ON DELETE SET NULL,

    CONSTRAINT ck_item_sessao_posicao CHECK (posicao > 0),

    CONSTRAINT uk_item_sessao_posicao UNIQUE (sessao_id, posicao)
);

CREATE INDEX idx_item_sessao_pergunta ON item_sessao (sessao_id, posicao);