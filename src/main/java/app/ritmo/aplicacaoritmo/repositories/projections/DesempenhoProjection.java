package app.ritmo.aplicacaoritmo.repositories.projections;

public interface DesempenhoProjection {
    String getCategoria();
    String getDisciplina();
    Long getTotal();
    Long getFaceis();
    Long getDificeis();
    Long getNaoLembrei();
}
