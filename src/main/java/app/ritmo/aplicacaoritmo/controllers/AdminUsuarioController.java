package app.ritmo.aplicacaoritmo.controllers;

import app.ritmo.aplicacaoritmo.dto.InativacaoCadastroInputDTO;
import app.ritmo.aplicacaoritmo.exceptions.NegocioException;
import app.ritmo.aplicacaoritmo.services.UsuarioAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {
    private final UsuarioAdminService usuarioAdminService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("paginaAtiva", "usuarios");
        model.addAttribute("usuarios", usuarioAdminService.listar());
        model.addAttribute("inativacaoInput", new InativacaoCadastroInputDTO(""));
        return "admin/usuarios";
    }

    @PostMapping("/{usuarioId}/inativar")
    public String inativar(
            @PathVariable Long usuarioId,
            @Valid @ModelAttribute("inativacaoInput") InativacaoCadastroInputDTO dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("erroUsuario", result.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("paginaAtiva", "usuarios");
            model.addAttribute("usuarios", usuarioAdminService.listar());
            model.addAttribute("usuarioSelecionadoId", usuarioId);
            return "admin/usuarios";
        }
        try {
            usuarioAdminService.inativar(usuarioId, dto);
            return "redirect:/admin/usuarios?atualizado";
        } catch (NegocioException ex) {
            model.addAttribute("erroUsuario", ex.getMessage());
            model.addAttribute("paginaAtiva", "usuarios");
            model.addAttribute("usuarios", usuarioAdminService.listar());
            return "admin/usuarios";
        }
    }

    @PostMapping("/{usuarioId}/reativar")
    public String reativar(@PathVariable Long usuarioId, Model model) {
        try {
            usuarioAdminService.reativar(usuarioId);
            return "redirect:/admin/usuarios?atualizado";
        } catch (NegocioException ex) {
            model.addAttribute("erroUsuario", ex.getMessage());
            model.addAttribute("paginaAtiva", "usuarios");
            model.addAttribute("usuarios", usuarioAdminService.listar());
            return "admin/usuarios";
        }
    }

    @PostMapping("/{usuarioId}/promover")
    public String promover(@PathVariable Long usuarioId, Model model) {
        try {
            usuarioAdminService.promover(usuarioId);
            return "redirect:/admin/usuarios?atualizado";
        } catch (NegocioException ex) {
            model.addAttribute("erroUsuario", ex.getMessage());
            model.addAttribute("paginaAtiva", "usuarios");
            model.addAttribute("usuarios", usuarioAdminService.listar());
            return "admin/usuarios";
        }
    }

    @PostMapping("/{usuarioId}/excluir")
    public String excluir(@PathVariable Long usuarioId, Model model) {
        try {
            usuarioAdminService.excluir(usuarioId);
            return "redirect:/admin/usuarios?excluido";
        } catch (NegocioException ex) {
            model.addAttribute("erroUsuario", ex.getMessage());
            model.addAttribute("paginaAtiva", "usuarios");
            model.addAttribute("usuarios", usuarioAdminService.listar());
            return "admin/usuarios";
        }
    }
}
