ALTER TABLE revisao_obrigatoria
    ALTER COLUMN agendada_para TYPE DATE USING agendada_para::date,
    ALTER COLUMN concluida_em TYPE DATE USING concluida_em::date;
