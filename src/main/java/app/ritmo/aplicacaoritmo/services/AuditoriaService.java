package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Auditoria;
import app.ritmo.aplicacaoritmo.repositories.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public void registrar(String usuarioEmail, AcaoAuditoria acao, String descricao) {
        Auditoria auditoria = new Auditoria(
                usuarioEmail,
                acao,
                descricao
        );

        repository.save(auditoria);
    }
}
