package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.ItemSessao;
import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.domain.Pergunta;
import app.ritmo.aplicacaoritmo.domain.SessaoEstudo;
import app.ritmo.aplicacaoritmo.domain.StatusSessao;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.BlocoInputDTO;
import app.ritmo.aplicacaoritmo.dto.BlocoOutputDTO;
import app.ritmo.aplicacaoritmo.dto.AutoavaliacaoInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerguntaSessaoDTO;
import app.ritmo.aplicacaoritmo.dto.RespostaPerguntaInputDTO;
import app.ritmo.aplicacaoritmo.dto.SessaoResumoDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.repositories.ItemSessaoRepository;
import app.ritmo.aplicacaoritmo.repositories.PerguntaRepository;
import app.ritmo.aplicacaoritmo.repositories.SessaoEstudoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final BlocoService blocoService;
    private final SecurityUtils securityUtils;
    private final SessaoEstudoRepository sessaoRepository;
    private final ItemSessaoRepository itemSessaoRepository;
    private final PerguntaRepository perguntaRepository;
    private final RevisaoService revisaoService;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    //cria a sessão de estudo
    @Transactional
    public Long criar(BlocoInputDTO dto) {
        BlocoOutputDTO configuracao = blocoService.configurar(dto);
        Usuario usuario = securityUtils.getUsuarioLogado();

        // cria sessão com base no tempo escolhido
        SessaoEstudo sessao = sessaoRepository.save(new SessaoEstudo(usuario, configuracao.duracaoMinutos()));
        auditoriaService.registrar(
                usuario.getEmail(),
                AcaoAuditoria.CRIAR_SESSAO,
                "Criou a sessão #" + sessao.getId() + " com duração de "
                        + configuracao.duracaoMinutos() + " minutos."
        );

        // busca e armazena os itens da sessão
        List<ItemSessao> itens = new ArrayList<>();
        int posicao = 1;

        for (BlocoOutputDTO.TopicoConfiguradoDTO topico : configuracao.topicos()) {
            List<Pergunta> perguntas = perguntaRepository
                    .findByTopico_IdAndTopico_Disciplina_IdAndTopico_Disciplina_Usuario_IdOrderByIdAsc(
                            topico.id(),
                            dto.disciplinaId(),
                            usuario.getId()
                    );

            if (perguntas.size() < topico.perguntasNoBloco()) {
                throw new NegocioException("O tópico \"" + topico.nome() + "\" não tem perguntas suficientes para iniciar este bloco.");
            }

            // embaralha as perguntas
            Collections.shuffle(perguntas);

            for (int i = 0; i < topico.perguntasNoBloco(); i++) {
                itens.add(new ItemSessao(sessao, perguntas.get(i), posicao++));
            }
        }

        itemSessaoRepository.saveAll(itens);
        return sessao.getId();
    }

    //resumo das informações da sessão
    @Transactional
    public SessaoResumoDTO buscarResumo(Long sessaoId) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());

        List<ItemSessao> itens = itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(sessaoId);

        Map<String, Long> perguntasPorTopico = itens.stream()
                .collect(Collectors.groupingBy(
                        ItemSessao::getTopicoNomeSnapshot,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<SessaoResumoDTO.TopicoResumoDTO> topicos = perguntasPorTopico.entrySet().stream()
                        .map(entrada -> new SessaoResumoDTO.TopicoResumoDTO(
                                entrada.getKey(),
                                Math.toIntExact(entrada.getValue())
                        ))
                        .toList();

        return new SessaoResumoDTO(
                sessao.getId(),
                sessao.getTipo(),
                sessao.getDuracaoMinutos(),
                sessao.getStatus(),
                sessao.getCriadaEm(),
                sessao.getInicioEm(),
                sessao.getFimEm(),
                itens.size(),
                topicos
        );
    }

    @Transactional
    public void iniciar(Long sessaoId) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());

        if (sessao.getStatus() == StatusSessao.EM_ANDAMENTO) {
            return;
        }

        if (sessao.getStatus() != StatusSessao.PENDENTE) {
            throw new NegocioException("Somente sessões pendentes podem ser iniciadas.");
        }

        sessao.iniciarEm(LocalDateTime.now(clock));
        auditoriaService.registrar(
                usuario.getEmail(),
                AcaoAuditoria.INICIAR_SESSAO,
                "Iniciou a sessão #" + sessao.getId() + "."
        );
    }

    @Transactional
    public Optional<PerguntaSessaoDTO> buscarPerguntaAtual(Long sessaoId) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());

        if (sessao.getStatus() != StatusSessao.EM_ANDAMENTO) {
            throw new NegocioException("Inicie a sessão antes de acessar as perguntas.");
        }

        List<ItemSessao> itens = itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(sessaoId);
        if (sessaoExpirada(sessao, LocalDateTime.now(clock))) {
            interromper(sessao, itens, true);
            return Optional.empty();
        }

        ItemSessao itemAtual = itens.stream()
                .filter(item -> item.getAutoavaliacao() == null)
                .findFirst()
                .orElseThrow(() -> new NegocioException("Não há perguntas pendentes nesta sessão."));

        long segundosRestantes = Math.max(
                0,
                (Duration.between(LocalDateTime.now(clock), sessao.prazoFinal()).toMillis() + 999) / 1000
        );
        boolean respostaEnviada = itemAtual.getRespondidaEm() != null;
        return Optional.of(new PerguntaSessaoDTO(
                sessao.getId(),
                itemAtual.getPosicao(),
                itens.size(),
                sessao.getDuracaoMinutos(),
                segundosRestantes,
                itemAtual.getEnunciadoSnapshot(),
                respostaEnviada ? itemAtual.getRespostaEstudante() : null,
                respostaEnviada ? itemAtual.getGabaritoSnapshot() : null,
                itemAtual.getAutoavaliacao()
        ));
    }

    @Transactional
    public StatusSessao registrarResposta(Long sessaoId, RespostaPerguntaInputDTO dto) {
        if (dto == null || dto.resposta() == null || dto.resposta().isBlank()) {
            throw new NegocioException("Escreva sua resposta antes de continuar.");
        }
        if (dto.resposta().length() > 20000) {
            throw new NegocioException("A resposta não pode ultrapassar 20.000 caracteres.");
        }

        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());
        List<ItemSessao> itens = obterItensAtivos(sessao);
        if (itens == null) {
            return sessao.getStatus();
        }

        ItemSessao itemAtual = obterItemAtual(itens);
        if (itemAtual.getRespondidaEm() != null) {
            throw new NegocioException("Sua resposta já foi registrada. Faça a autoavaliação para continuar.");
        }

        itemAtual.registrarResposta(dto.resposta(), LocalDateTime.now(clock));
        return sessao.getStatus();
    }

    @Transactional
    public StatusSessao registrarAutoavaliacao(Long sessaoId, AutoavaliacaoInputDTO dto) {
        if (dto == null || dto.autoavaliacao() == null) {
            throw new NegocioException("Escolha como foi responder à pergunta.");
        }

        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());
        List<ItemSessao> itens = obterItensAtivos(sessao);
        if (itens == null) {
            return sessao.getStatus();
        }

        ItemSessao itemAtual = obterItemAtual(itens);
        if (itemAtual.getRespondidaEm() == null) {
            throw new NegocioException("Registre sua resposta antes de fazer a autoavaliação.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        itemAtual.registrarAutoavaliacao(dto.autoavaliacao(), agora);

        boolean todasAvaliadas = itens.stream()
                .allMatch(item -> item.getId().equals(itemAtual.getId())
                        || item.getAutoavaliacao() != null);
        if (todasAvaliadas) {
            sessao.concluirEm(agora);
            revisaoService.agendarPrimeirasRevisoes(sessao, agora.toLocalDate());
            revisaoService.agendarProximasRevisoes(sessao, agora.toLocalDate());
            auditoriaService.registrar(
                    usuario.getEmail(),
                    AcaoAuditoria.CONCLUIR_SESSAO,
                    "Concluiu a sessão #" + sessao.getId() + "."
            );
        }
        return sessao.getStatus();
    }

    @Transactional
    public void interromper(Long sessaoId) {
        Usuario usuario = securityUtils.getUsuarioLogado();
        SessaoEstudo sessao = buscarSessaoDoUsuario(sessaoId, usuario.getId());
        if (sessao.getStatus() != StatusSessao.EM_ANDAMENTO) {
            throw new NegocioException("Somente uma sessão em andamento pode ser interrompida.");
        }

        interromper(
                sessao,
                itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(sessaoId),
                false
        );
    }

    private List<ItemSessao> obterItensAtivos(SessaoEstudo sessao) {
        if (sessao.getStatus() != StatusSessao.EM_ANDAMENTO) {
            throw new NegocioException("Esta sessão não está em andamento.");
        }

        List<ItemSessao> itens =
                itemSessaoRepository.findBySessao_IdOrderByPosicaoAsc(sessao.getId());
        if (sessaoExpirada(sessao, LocalDateTime.now(clock))) {
            interromper(sessao, itens, true);
            return null;
        }
        return itens;
    }

    private ItemSessao obterItemAtual(List<ItemSessao> itens) {
        return itens.stream()
                .filter(item -> item.getAutoavaliacao() == null)
                .findFirst()
                .orElseThrow(() -> new NegocioException("Não há perguntas pendentes nesta sessão."));
    }

    private boolean sessaoExpirada(SessaoEstudo sessao, LocalDateTime agora) {
        return sessao.prazoFinal() == null || !agora.isBefore(sessao.prazoFinal());
    }

    private void interromper(SessaoEstudo sessao, List<ItemSessao> itens, boolean prazoExpirado) {
        sessao.interromper();
        itens.forEach(ItemSessao::reiniciarTentativa);
        revisaoService.liberarRevisoesDaSessao(sessao);
        auditoriaService.registrar(
                sessao.getUsuario().getEmail(),
                AcaoAuditoria.INTERROMPER_SESSAO,
                "Interrompeu a sessão #" + sessao.getId()
                        + (prazoExpirado ? " após o prazo expirar." : ".")
        );
    }

    private SessaoEstudo buscarSessaoDoUsuario(Long sessaoId, Long usuarioId) {
        return sessaoRepository.findByIdAndUsuario_Id(sessaoId, usuarioId).orElseThrow(() -> new NegocioException("Sessão não encontrada."));
    }
}