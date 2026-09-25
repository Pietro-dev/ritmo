package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.FraseOutputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class QuotableClientService {

    /*
    * Esse service busca uma frase aleatória por meio da api quotable
    */

    private static final Logger log = LoggerFactory.getLogger(QuotableClientService.class);

    private final RestClient restClient;

    public QuotableClientService(@Qualifier("quotableRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<FraseOutputDTO> buscarFraseAleatoria() {
        try {
            FraseOutputDTO frase = restClient.get()
                    .uri("/random")
                    .retrieve()
                    .body(FraseOutputDTO.class);

            if (frase == null || frase.frase() == null || frase.autor().isBlank()) {
                log.warn("A Quotable não retornou conteúdo para a frase.");
                return Optional.empty();
            }

            return Optional.of(frase);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar a Quotable: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
