package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.domain.StatusCadastro;
import app.ritmo.aplicacaoritmo.domain.Usuario;
import app.ritmo.aplicacaoritmo.dto.UsuarioInputDTO;
import app.ritmo.aplicacaoritmo.repositories.UsuarioRepository;
import app.ritmo.aplicacaoritmo.services.FraseMotivacionalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
public class AuthController {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private FraseMotivacionalService fraseMotivacionalService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("frase", fraseMotivacionalService.buscarFraseETraduzir().orElse(null));
        return "login";
    }

    @GetMapping("/cadastro")
    public String exibirFormularioCadastro(Model model) {
        // Inicializa o record com valores padrão para o formulário Thymeleaf
        model.addAttribute("usuarioInputDTO", new UsuarioInputDTO("", "", "", false));
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("usuarioInputDTO") UsuarioInputDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "cadastro";
        }

        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            bindingResult.rejectValue("email", "error.cadastroDTO", "Já existe um usuário cadastrado com este e-mail.");
            return "cadastro";
        }

        Usuario novoUsuario = new Usuario(dto.nome(), dto.email(), encoder.encode(dto.senha()), Role.ROLE_ESTUDANTE, StatusCadastro.ATIVO, LocalDateTime.now());
        usuarioRepository.save(novoUsuario);
        return "redirect:/login?cadastrado=true";
    }

    @GetMapping("/termos-de-uso")
    public String termosDeUso() {
        return "termos-de-uso";
    }

    @GetMapping("/politica-de-privacidade")
    public String politicaPrivacidade() {
        return "politica-de-privacidade";
    }
}
