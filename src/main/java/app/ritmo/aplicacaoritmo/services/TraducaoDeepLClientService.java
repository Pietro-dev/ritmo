package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.DeepLOutputDTO;
import app.ritmo.aplicacaoritmo.dto.DeepLInputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Service
public class TraducaoDeepLClientService {

    /*
    * Este service irá traduzir as frases do quotable para português BR.
    * Muitas das frases do quotable são em inglês... Isso não faz sentido no contexto da aplicação
    */

    private static final Logger log =
            LoggerFactory.getLogger(TraducaoDeepLClientService.class);

    private final RestClient restClient;
    private final String authKey;

    public TraducaoDeepLClientService(
            @Qualifier("deeplRestClient") RestClient restClient,
            @Value("${integrations.deepl.auth-key:}") String authKey
    ) {
        this.restClient = restClient;
        this.authKey = authKey;
    }

    public Optional<String> traduzirParaPortugues(String texto) {
        if (authKey.isBlank()) {
            log.warn("DEEPL_AUTH_KEY não está configurada.");
            return Optional.empty();
        }

        try {
            DeepLOutputDTO response = restClient.post()
                    .uri("/v2/translate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "DeepL-Auth-Key " + authKey)
                    .body(new DeepLInputDTO(List.of(texto), "PT-BR"))
                    .retrieve()
                    .body(DeepLOutputDTO.class);

            if (response == null
                    || response.translations() == null
                    || response.translations().isEmpty()) {
                log.warn("A DeepL não retornou uma tradução.");
                return Optional.empty();
            }

            String traducao = response.translations().getFirst().text();
            if (traducao == null || traducao.isBlank()) {
                log.warn("A DeepL retornou uma tradução vazia.");
                return Optional.empty();
            }

            return Optional.of(traducao);
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar a DeepL: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
