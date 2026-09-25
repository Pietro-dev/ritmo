package app.ritmo.aplicacaoritmo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraseOutputDTO (String frase, String autor){
}
