UPDATE usuario
SET data_cadastro = data_cadastro - INTERVAL '3 hours',
    termo_aceito_em = termo_aceito_em - INTERVAL '3 hours',
    inativado_em = CASE
        WHEN inativado_em IS NULL THEN NULL
        ELSE inativado_em - INTERVAL '3 hours'
    END;

UPDATE auditorias
SET data_hora = data_hora - INTERVAL '3 hours';

UPDATE sessao_estudo
SET criada_em = criada_em - INTERVAL '3 hours',
    inicio_em = CASE
        WHEN inicio_em IS NULL THEN NULL
        ELSE inicio_em - INTERVAL '3 hours'
    END,
    fim_em = CASE
        WHEN fim_em IS NULL THEN NULL
        ELSE fim_em - INTERVAL '3 hours'
    END;

UPDATE item_sessao
SET respondida_em = CASE
        WHEN respondida_em IS NULL THEN NULL
        ELSE respondida_em - INTERVAL '3 hours'
    END,
    autoavaliada_em = CASE
        WHEN autoavaliada_em IS NULL THEN NULL
        ELSE autoavaliada_em - INTERVAL '3 hours'
    END;

UPDATE revisao_obrigatoria
SET agendada_para = agendada_para - INTERVAL '3 hours',
    concluida_em = CASE
        WHEN concluida_em IS NULL THEN NULL
        ELSE concluida_em - INTERVAL '3 hours'
    END;
