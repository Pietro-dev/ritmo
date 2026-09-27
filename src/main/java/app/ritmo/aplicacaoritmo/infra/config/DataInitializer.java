package app.ritmo.aplicacaoritmo.infra.config;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //valores pré definidos no ambiente de dev. Em prod, os valores das variávei de ambiente serão utilizados.
    @Value("${APP_ADMIN_NOME:admin}")
    private String adminNome;

    @Value("${APP_ADMIN_SENHA:123456}")
    private String adminSenha;

    @Value("${APP_ADMIN_EMAIL:admin@email.com}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        if(usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome(adminNome);
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminSenha));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setStatus(StatusCadastro.ATIVO);
            admin.setTermoAceitoEm(LocalDateTime.now());

            usuarioRepository.save(admin);
            System.out.println(">>> Usuário ADMIN inicializado com sucesso!");
        }
    }
}
