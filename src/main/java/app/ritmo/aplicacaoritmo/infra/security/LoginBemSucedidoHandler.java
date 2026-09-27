package app.ritmo.aplicacaoritmo.infra.security;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.services.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginBemSucedidoHandler implements AuthenticationSuccessHandler {
    /*
    * O SpringBoot, por meio do Spring Security, processa internamente o Login.
    * Sendo assim, as boas práticas recomendam usar um handler para adicionar um comportamento personalizado após login,
    * sem modificar ou substituri o mecanismo de segurança do Spring.
    * Nosso fluxo é: usuario envia login e senha > spring securit valida > se o login for bem sucedido o loginBemSucedidoHandler é executado
    * após isso, o handler insere um registro de login na tabela auditoria e o usuário vai para o /dashboard
    */

    private final AuditoriaService auditoriaService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)

    throws IOException, ServletException {
        auditoriaService.registrar(authentication.getName(), AcaoAuditoria.LOGIN, "Usuário realizou login");

        response.sendRedirect("/dashboard");
    }

}
