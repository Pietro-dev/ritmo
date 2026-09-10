package app.ritmo.aplicacaoritmo.web;

import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
    * Essa classe vigia todos os controllers.
    * Sempre que um controller, ou um use case disparar uma NegocioException,
    * este componente irá capturar o erro gerando um log de aviso e renderizando a erro.html
    */

    // captura erros de validação e regras do Estudaí
    @ExceptionHandler(NegocioException.class)
    public ModelAndView handleNegocioException(NegocioException ex){
        log.warn("Alerta de negócio: {}", ex.getMessage());

        ModelAndView mv = new ModelAndView("erro");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    // captura qualquer outro erro inesperado
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex){
        log.error("Erro crítico inesperado", ex);

        ModelAndView mv = new ModelAndView("erro");
        mv.addObject("message", "Ocorreu um erro interno inesperado");
        return mv;
    }
}
