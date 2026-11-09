package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.dto.FraseOutputDTO;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FraseMotivacionalServiceTest {

    @Test
    void usaFraseLocalQuandoServicoExternoFalha() {
        var quotable = mock(QuotableClientService.class);
        var traducao = mock(TraducaoDeepLClientService.class);
        when(quotable.buscarFraseAleatoria()).thenReturn(Optional.empty());

        var service = new FraseMotivacionalService(quotable, traducao);
        FraseOutputDTO frase = service.buscarFraseETraduzir().orElseThrow();

        assertTrue(frase.frase().length() > 0);
        assertEquals("Ritmo", frase.autor());
        verify(traducao, org.mockito.Mockito.never()).traduzirParaPortugues(
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void usaFraseLocalQuandoTraducaoExternaFalha() {
        var quotable = mock(QuotableClientService.class);
        var traducao = mock(TraducaoDeepLClientService.class);
        when(quotable.buscarFraseAleatoria())
                .thenReturn(Optional.of(new FraseOutputDTO("Keep going", "Author")));
        when(traducao.traduzirParaPortugues("Keep going")).thenReturn(Optional.empty());

        var service = new FraseMotivacionalService(quotable, traducao);
        FraseOutputDTO frase = service.buscarFraseETraduzir().orElseThrow();

        assertTrue(frase.frase().length() > 0);
        assertEquals("Ritmo", frase.autor());
    }
}
