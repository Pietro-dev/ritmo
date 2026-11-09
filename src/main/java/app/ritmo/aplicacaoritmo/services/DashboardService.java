package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.DesempenhoDashboardDTO;
import app.ritmo.aplicacaoritmo.dto.ResumoDashboardDTO;
import app.ritmo.aplicacaoritmo.dto.PeriodoDashboard;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.RevisaoObrigatoriaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import app.ritmo.aplicacaoritmo.repositories.projections.DesempenhoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SecurityUtils securityUtils;
    private final SessaoEstudoRepository sessaoRepository;
    private final RevisaoObrigatoriaRepository revisaoRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ResumoDashboardDTO carregarResumo(PeriodoDashboard periodo) {
        Long usuarioId = securityUtils.getUsuarioLogado().getId();
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime inicio = periodo.inicio(hoje);
        LocalDateTime fim = periodo.fim(hoje);
        var sessoes = sessaoRepository.resumirSessoesConcluidas(usuarioId, inicio, fim);
        List<DesempenhoDashboardDTO> porDisciplina =
                converter(sessaoRepository.desempenhoPorDisciplina(usuarioId, inicio, fim));
        List<DesempenhoDashboardDTO> porTopico =
                converter(sessaoRepository.desempenhoPorTopico(usuarioId, inicio, fim));

        long faceis = porDisciplina.stream().mapToLong(DesempenhoDashboardDTO::faceis).sum();
        long dificeis = porDisciplina.stream().mapToLong(DesempenhoDashboardDTO::dificeis).sum();
        long naoLembrei = porDisciplina.stream()
                .mapToLong(DesempenhoDashboardDTO::naoLembrei)
                .sum();
        long revisoesPendentes = revisaoRepository.countByUsuario_IdAndConcluidaEmIsNull(usuarioId);
        long revisoesDisponiveis = revisaoRepository
                .countByUsuario_IdAndConcluidaEmIsNullAndSessaoRevisaoIsNullAndAgendadaParaLessThanEqual(
                        usuarioId,
                        hoje
                );

        return new ResumoDashboardDTO(
                valor(sessoes.getSessoesConcluidas()),
                valor(sessoes.getMinutosEstudados()),
                faceis + dificeis + naoLembrei,
                faceis,
                dificeis,
                naoLembrei,
                revisoesPendentes,
                revisoesDisponiveis,
                porDisciplina,
                porTopico
        );
    }

    private List<DesempenhoDashboardDTO> converter(List<DesempenhoProjection> resultados) {
        return resultados.stream()
                .map(resultado -> new DesempenhoDashboardDTO(
                        resultado.getCategoria(),
                        resultado.getDisciplina(),
                        valor(resultado.getTotal()),
                        valor(resultado.getFaceis()),
                        valor(resultado.getDificeis()),
                        valor(resultado.getNaoLembrei())
                ))
                .toList();
    }

    private long valor(Long valor) {
        return valor == null ? 0 : valor;
    }
}
