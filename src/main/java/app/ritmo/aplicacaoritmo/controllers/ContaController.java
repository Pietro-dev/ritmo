package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.ExclusaoContaInputDTO;
import app.ritmo.aplicacaoritmo.dto.PerfilInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.domain.Role;
import app.ritmo.aplicacaoritmo.infra.security.SecurityUtils;
import app.ritmo.aplicacaoritmo.services.ContaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/conta")
public class ContaController {

    private final ContaService contaService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String exibir(Model model) {
        var usuario = securityUtils.getUsuarioLogado();
        model.addAttribute("usuario", usuario);
        model.addAttribute("ehAdmin", usuario.getRole() == Role.ROLE_ADMIN);
        model.addAttribute("perfilInput", new PerfilInputDTO(usuario.getNome(), "", "", ""));
        model.addAttribute("exclusaoInput", new ExclusaoContaInputDTO("", false));
        model.addAttribute("paginaAtiva", "conta");
        return "conta/perfil";
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(
            @Valid @ModelAttribute("perfilInput") PerfilInputDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            prepararModelo(model);
            return "conta/perfil";
        }

        try {
            contaService.atualizarPerfil(dto);
            return "redirect:/conta?atualizada";
        } catch (NegocioException ex) {
            model.addAttribute("erroPerfil", ex.getMessage());
            prepararModelo(model);
            return "conta/perfil";
        }
    }

    @PostMapping("/excluir")
    public String excluirConta(
            @Valid @ModelAttribute("exclusaoInput") ExclusaoContaInputDTO dto,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "erroExclusao",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
            prepararModelo(model);
            return "conta/perfil";
        }

        try {
            contaService.excluirConta(dto);
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            return "redirect:/login?contaExcluida";
        } catch (NegocioException ex) {
            model.addAttribute("erroExclusao", ex.getMessage());
            prepararModelo(model);
            return "conta/perfil";
        }
    }

    private void prepararModelo(Model model) {
        var usuario = securityUtils.getUsuarioLogado();
        model.addAttribute("paginaAtiva", "conta");
        model.addAttribute("usuario", usuario);
        model.addAttribute("ehAdmin", usuario.getRole() == Role.ROLE_ADMIN);
        model.addAttribute("exclusaoInput", new ExclusaoContaInputDTO("", false));
    }
}
