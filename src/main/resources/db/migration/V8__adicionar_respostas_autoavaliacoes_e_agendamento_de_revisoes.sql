ALTER TABLE sessao_estudo
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'ESTUDO';

ALTER TABLE sessao_estudo
    ADD CONSTRAINT ck_sessao_estudo_tipo
    CHECK (tipo IN ('ESTUDO', 'REVISAO'));

ALTER TABLE item_sessao
    ADD COLUMN resposta_estudante TEXT NULL,
    ADD COLUMN autoavaliacao VARCHAR(20) NULL,
    ADD COLUMN respondida_em TIMESTAMP NULL,
    ADD COLUMN autoavaliada_em TIMESTAMP NULL,
    ADD COLUMN topico_id_snapshot BIGINT NULL,
    ADD COLUMN disciplina_nome_snapshot VARCHAR(255) NULL;

UPDATE item_sessao item
SET topico_id_snapshot = pergunta.topico_id,
    disciplina_nome_snapshot = disciplina.nome
FROM pergunta
JOIN topico ON topico.id = pergunta.topico_id
JOIN disciplina ON disciplina.id = topico.disciplina_id
WHERE item.pergunta_id = pergunta.id;

UPDATE item_sessao
SET disciplina_nome_snapshot = 'Disciplina não disponível'
WHERE disciplina_nome_snapshot IS NULL;

ALTER TABLE item_sessao
    ALTER COLUMN disciplina_nome_snapshot SET NOT NULL,
    ADD CONSTRAINT ck_item_sessao_autoavaliacao
        CHECK (autoavaliacao IS NULL OR autoavaliacao IN ('FACIL', 'DIFICIL', 'NAO_LEMBRO')),
    ADD CONSTRAINT ck_item_sessao_resposta_avaliacao
        CHECK (autoavaliacao IS NULL OR respondida_em IS NOT NULL);

CREATE TABLE revisao_obrigatoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    sessao_origem_id BIGINT NOT NULL,
    sessao_revisao_id BIGINT NULL,
    topico_id_snapshot BIGINT NOT NULL,
    topico_nome_snapshot VARCHAR(50) NOT NULL,
    disciplina_nome_snapshot VARCHAR(255) NOT NULL,
    etapa VARCHAR(20) NOT NULL,
    agendada_para TIMESTAMP NOT NULL,
    concluida_em TIMESTAMP NULL,

    CONSTRAINT fk_revisao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    CONSTRAINT fk_revisao_sessao_origem
        FOREIGN KEY (sessao_origem_id) REFERENCES sessao_estudo(id) ON DELETE CASCADE,
    CONSTRAINT fk_revisao_sessao_revisao
        FOREIGN KEY (sessao_revisao_id) REFERENCES sessao_estudo(id) ON DELETE SET NULL,
    CONSTRAINT ck_revisao_etapa
        CHECK (etapa IN ('HORAS_24', 'DIAS_7', 'DIAS_30', 'MESES_2')),
    CONSTRAINT uk_revisao_origem_topico_etapa
        UNIQUE (sessao_origem_id, topico_id_snapshot, etapa)
);

CREATE INDEX idx_revisao_usuario_pendentes
    ON revisao_obrigatoria (usuario_id, agendada_para)
    WHERE concluida_em IS NULL;

CREATE INDEX idx_revisao_sessao_revisao
    ON revisao_obrigatoria (sessao_revisao_id);
