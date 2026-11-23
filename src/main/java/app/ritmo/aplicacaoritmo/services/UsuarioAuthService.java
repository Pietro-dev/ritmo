package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import app.ritmo.aplicacaoritmo.infra.security.EmailHash;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAuthService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioAuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

//    converte o Usuario em User, formato que o Spring security utiliza
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(usuario.getRole().name())
                .disabled(usuario.getStatus() != StatusCadastro.ATIVO)
                .build();
    }
}
