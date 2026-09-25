package app.ritmo.aplicacaoritmo.infra.security;

import app.ritmo.aplicacaoritmo.domain.AcaoAuditoria;
import app.ritmo.aplicacaoritmo.services.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogoutBemSucedidoHandler implements LogoutSuccessHandler {
    /*
    * Esse handler tem a lógica bem parecida com o de login.
    * Ele cria um comportamento personalizado para o logout da aplicação.
    * no caso, nosso comportamento personalizado é:
    * - esccrever ação de logout na tabela de auditoria
    * - redirecionar usuário para tela de logout.
    * em alguns casos, não é possível garantir que houve logout. Casos como:
    * - queda de internet;
    * - fechar navegador;
    * - fechou a aba...
    */

    private final AuditoriaService auditoriaService;

    // metodo herdado da interface LogoutSuccessHandler. Sua implantação é obrigatória
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            auditoriaService.registrar(authentication.getName(), AcaoAuditoria.LOGOUT, "Usuário realizou logout");
        }

        response.sendRedirect("/login?logout");
    }

}
