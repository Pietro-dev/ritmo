package app.ritmo.aplicacaoritmo.dto;

import java.util.List;

public record DeepLOutputDTO(List<Translation> translations) {
    public record Translation(String text) {
    }
}