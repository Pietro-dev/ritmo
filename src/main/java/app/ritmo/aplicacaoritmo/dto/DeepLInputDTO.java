package app.ritmo.aplicacaoritmo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DeepLInputDTO (
        List<String> text,
        @JsonProperty("target_lang")
        String targetLang
) {
}
