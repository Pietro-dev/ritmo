package app.estudai.aplicacaoestudai.exceptions;

public class NegocioException extends RuntimeException{
    /*
    * Essa classe será disparada nos usecases sempre que uma regra for violada.
    * Ex: disciplinas duplicadas...
    */
    public NegocioException(String mensagem){
        super(mensagem);
    }
}
