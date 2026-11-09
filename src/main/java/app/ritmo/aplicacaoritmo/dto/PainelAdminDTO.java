package app.ritmo.aplicacaoritmo.dto;

import java.util.List;

public record PainelAdminDTO(
        long usuariosAtivos,
        long novosUsuarios,
        long sessoesConcluidas,
        long perguntasRespondidas,
        long disciplinas,
        long topicos,
        long perguntas,
        long topicosSemPerguntas,
        long revisoesPendentes,
        long revisoesConcluidas,
        List<AtividadeAdminDTO> atividade,
        List<AuditoriaAdminDTO> auditoriasRecentes
) {
}
