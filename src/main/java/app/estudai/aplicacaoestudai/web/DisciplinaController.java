package app.estudai.aplicacaoestudai.web;

import app.estudai.aplicacaoestudai.dto.DisciplinaInputDTO;
import app.estudai.aplicacaoestudai.dto.DisciplinaOutputDTO;
import app.estudai.aplicacaoestudai.exceptions.NegocioException;
import app.estudai.aplicacaoestudai.services.DisciplinaServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/disciplinas")
public class DisciplinaController {
    @Autowired
    DisciplinaServices service;

    @GetMapping
    public String listar(Model model){
        model.addAttribute("disciplinas", service.listar());
        model.addAttribute("disciplinaInput", new DisciplinaInputDTO(""));

        return "disciplinas/lista";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("disciplinaInput") DisciplinaInputDTO dto,
                            BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("disciplinas", service.listar());
            return "disciplinas/lista";
        }
        try {
            service.cadastrar(dto);
        } catch (NegocioException ex) {
            result.rejectValue("nome", "duplicado", ex.getMessage());
            model.addAttribute("disciplinas", service.listar());
            return "disciplinas/lista";
        }
        return "redirect:/disciplinas";
    }

    @GetMapping("/{id}/editar")
    public String exibirEdicao(@PathVariable Long id, Model model) {
        // Busca os dados atuais da disciplina para exibir no formulário
        DisciplinaOutputDTO dto = service.buscarPeloId(id);

        model.addAttribute("disciplinaId", id);
        // Passa o DTO preenchido com o nome atual
        model.addAttribute("disciplinaInput", new DisciplinaInputDTO(dto.nome()));

        return "disciplinas/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("disciplinaInput") DisciplinaInputDTO dto, BindingResult result, Model model) {
        // Valida se o usuário limpou o campo ou digitou algo inválido
        if (result.hasErrors()) {
            model.addAttribute("disciplinaId", id);
            return "disciplinas/editar";
        }

        try {
            // Tenta executar a alteração no Use Case
            service.atualizar(id, dto);
        } catch (NegocioException ex) {
            // Se houver erro de negócio vincula o erro ao campo 'nome'
            result.rejectValue("nome", "duplicado", ex.getMessage());
            model.addAttribute("disciplinaId", id);
            return "disciplinas/editar";
        }

        return "redirect:/disciplinas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id){
        service.excluir(id);
        return "redirect:/disciplinas";
    }
}
