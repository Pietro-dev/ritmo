package app.ritmo.aplicacaoritmo.services;

import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UsuarioAuthService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

//    converte o Usuario em User, formato que o Spring security utiliza
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email) .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole().name()))
        );
    }
}
