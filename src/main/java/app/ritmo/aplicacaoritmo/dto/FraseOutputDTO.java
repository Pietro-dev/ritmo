package app.ritmo.aplicacaoritmo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraseOutputDTO (
        @JsonProperty("content")String frase,
        @JsonProperty("author")String autor)
{ }
