package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.AtividadeAdminDTO;
import app.ritmo.aplicacaoritmo.dto.AuditoriaAdminDTO;
import app.ritmo.aplicacaoritmo.dto.PainelAdminDTO;
import app.ritmo.aplicacaoritmo.dto.PeriodoDashboard;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.DisciplinaRepository;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.RevisaoObrigatoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import app.ritmo.aplicacaoritmo.repositories.TopicoRepository;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import app.ritmo.aplicacaoritmo.repositories.projections.AtividadeAdminProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PainelAdminService {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("pt-BR"));

    private final UsuarioRepository usuarioRepository;
    private final SessaoEstudoRepository sessaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final TopicoRepository topicoRepository;
    private final PerguntaRepository perguntaRepository;
    private final RevisaoObrigatoriaRepository revisaoRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PainelAdminDTO carregar(PeriodoDashboard periodo) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime inicio = periodo.inicio(hoje);
        LocalDateTime fim = periodo.fim(hoje);

        long novosUsuarios = inicio == null
                ? usuarioRepository.count()
                : usuarioRepository.countByDataCadastroGreaterThanEqualAndDataCadastroLessThan(inicio, fim);
        long revisoesConcluidas = inicio == null
                ? revisaoRepository.countByConcluidaEmIsNotNull()
                : revisaoRepository.countConcluidasNoPeriodo(inicio.toLocalDate(), fim.toLocalDate());

        return new PainelAdminDTO(
                usuarioRepository.countByStatus(StatusCadastro.ATIVO),
                novosUsuarios,
                sessaoRepository.contarSessoesConcluidasNaPlataforma(inicio, fim),
                sessaoRepository.contarPerguntasRespondidasNaPlataforma(inicio, fim),
                disciplinaRepository.count(),
                topicoRepository.count(),
                perguntaRepository.count(),
                topicoRepository.countTopicosSemPerguntas(),
                revisaoRepository.countByConcluidaEmIsNull(),
                revisoesConcluidas,
                converterAtividade(sessaoRepository.atividadeDaPlataforma(inicio, fim), inicio == null),
                auditoriaRepository.buscarAcoesAdministrativasRecentes().stream()
                        .map(auditoria -> new AuditoriaAdminDTO(
                                auditoria.getDataHora(),
                                auditoria.getAcao().toLowerCase(Locale.ROOT).replace('_', ' ')
                        ))
                        .toList()
        );
    }

    private List<AtividadeAdminDTO> converterAtividade(
            List<AtividadeAdminProjection> resultados,
            boolean agruparPorMes
    ) {
        long maximo = resultados.stream()
                .mapToLong(resultado -> Math.max(valor(resultado.getCadastros()), valor(resultado.getSessoes())))
                .max()
                .orElse(0);
        DateTimeFormatter formato = agruparPorMes ? FORMATO_MES : FORMATO_DIA;

        return resultados.stream()
                .map(resultado -> {
                    long cadastros = valor(resultado.getCadastros());
                    long sessoes = valor(resultado.getSessoes());
                    return new AtividadeAdminDTO(
                            resultado.getPeriodo().format(formato),
                            cadastros,
                            sessoes,
                            percentual(cadastros, maximo),
                            percentual(sessoes, maximo)
                    );
                })
                .toList();
    }

    private int percentual(long quantidade, long maximo) {
        return maximo == 0 ? 0 : (int) Math.round(quantidade * 100.0 / maximo);
    }

    private long valor(Long valor) {
        return valor == null ? 0 : valor;
    }
}
