package app.ritmo.aplicacaoritmo.services;


import app.ritmo.aplicacaoritmo.dto.FraseOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class FraseMotivacionalService {

//    service que centraliza busca da frase e tradução.


    // frases para serem usadas quando a API falhar
    private static final List<FraseOutputDTO> FRASES_LOCAIS = List.of(
            new FraseOutputDTO("Cada sessão de estudo é um passo a mais na direção do seu objetivo.", "Ritmo"),
            new FraseOutputDTO("Aprender um pouco hoje torna o próximo desafio mais possível.", "Ritmo"),
            new FraseOutputDTO("A constância transforma pequenos esforços em grandes avanços.", "Ritmo"),
            new FraseOutputDTO("Não precisa saber tudo agora; continue avançando uma pergunta por vez.", "Ritmo")
    );

    private final QuotableClientService quotableClientService;
    private final TraducaoDeepLClientService traducaoDeepLClientService;

    public Optional<FraseOutputDTO> buscarFraseETraduzir() {
        Optional<FraseOutputDTO> fraseExterna = quotableClientService.buscarFraseAleatoria();
        if (fraseExterna.isEmpty()) {
            return Optional.of(buscarFraseLocal());
        }

        FraseOutputDTO original = fraseExterna.get();
        Optional<String> traducao = traducaoDeepLClientService
                .traduzirParaPortugues(original.frase());

        if (traducao.isEmpty()) {
            return Optional.of(buscarFraseLocal());
        }

        return Optional.of(new FraseOutputDTO(traducao.get(), original.autor()));
    }

    private FraseOutputDTO buscarFraseLocal() {
        int indice = ThreadLocalRandom.current().nextInt(FRASES_LOCAIS.size());
        return FRASES_LOCAIS.get(indice);
    }
}
