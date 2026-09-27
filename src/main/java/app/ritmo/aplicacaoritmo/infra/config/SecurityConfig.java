package app.ritmo.aplicacaoritmo.infra.config;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.infra.security.LoginBemSucedidoHandler;
import app.ritmo.aplicacaoritmo.infra.security.LogoutBemSucedidoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // injeção dos handlers de login e logout
    private final LoginBemSucedidoHandler loginBemSucedidoHandler;
    private final LogoutBemSucedidoHandler logoutBemSucedidoHandler;

    // bean de criptografia, disponível para toda a aplicação
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/cadastro", "/login", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority(Role.ROLE_ADMIN.name())
                .anyRequest()
                .authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        // adicao do handler de login na sequencia de filtros de segurança
                        .successHandler(loginBemSucedidoHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // adicao do handler de logout na sequencia de filtros de segurança
                        .logoutSuccessHandler(logoutBemSucedidoHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build(); }
}
