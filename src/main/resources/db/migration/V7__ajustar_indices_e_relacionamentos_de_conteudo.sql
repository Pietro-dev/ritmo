DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM topico
        GROUP BY disciplina_id, LOWER(nome)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Não é possível criar uk_topico_disciplina_nome: existem tópicos duplicados por disciplina.';
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_topico_disciplina_nome
    ON topico (disciplina_id, LOWER(nome));

CREATE INDEX IF NOT EXISTS idx_pergunta_topico_id
    ON pergunta (topico_id);

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'topico'::regclass
          AND confrelid = 'disciplina'::regclass
          AND contype = 'f'
    LOOP
        EXECUTE format(
            'ALTER TABLE topico DROP CONSTRAINT %I',
            constraint_name
        );
    END LOOP;

    ALTER TABLE topico
        ADD CONSTRAINT fk_topico_disciplina
        FOREIGN KEY (disciplina_id)
        REFERENCES disciplina(id)
        ON DELETE CASCADE;

    FOR constraint_name IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'pergunta'::regclass
          AND confrelid = 'topico'::regclass
          AND contype = 'f'
    LOOP
        EXECUTE format(
            'ALTER TABLE pergunta DROP CONSTRAINT %I',
            constraint_name
        );
    END LOOP;

    ALTER TABLE pergunta
        ADD CONSTRAINT fk_pergunta_topico
        FOREIGN KEY (topico_id)
        REFERENCES topico(id)
        ON DELETE CASCADE;
END
$$;
