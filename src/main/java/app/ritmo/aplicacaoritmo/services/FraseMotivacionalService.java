package app.ritmo.aplicacaoritmo.services;


import app.ritmo.aplicacaoritmo.dto.FraseOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FraseMotivacionalService {

//    service que centraliza busca da frase e tradução.

    private final QuotableClientService quotableClientService;
    private final TraducaoDeepLClientService traducaoDeepLClientService;

    public Optional<FraseOutputDTO> buscarFraseETraduzir() {
        return quotableClientService.buscarFraseAleatoria()
                .map(fraseOriginal -> {
                    String texto = traducaoDeepLClientService
                            .traduzirParaPortugues(fraseOriginal.frase())
                            .orElse(fraseOriginal.frase());

                    return new FraseOutputDTO(texto, fraseOriginal.autor());
                });
    }
}
