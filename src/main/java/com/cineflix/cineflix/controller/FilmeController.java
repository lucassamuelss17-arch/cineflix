package com.cineflix.cineflix.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.cineflix.cineflix.service.TmdbImportService;
import com.cineflix.cineflix.model.Filme;
import com.cineflix.cineflix.service.FilmeService;

@Controller
public class FilmeController {

    private final FilmeService filmeService;
    private final TmdbImportService tmdbImportService;	

    public FilmeController(
            FilmeService filmeService,
            TmdbImportService tmdbImportService) {

        this.filmeService = filmeService;
        this.tmdbImportService = tmdbImportService;
    }

    // Página inicial
    @GetMapping("/")
    public String inicio(
            @RequestParam(required = false) String busca,
            Model model) {

        if (busca != null && !busca.trim().isEmpty()) {
            model.addAttribute("filmes", filmeService.pesquisar(busca));
        } else {
            model.addAttribute("filmes", filmeService.listarTodos());
        }

        model.addAttribute("filme", new Filme());
        model.addAttribute("busca", busca);

        return "index";
    }

    // Abrir formulário
    @GetMapping("/novo")
    public String novoFilme(Model model) {
        model.addAttribute("filme", new Filme());
        return "formulario";
    }

    // Salvar filme
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Filme filme) {
        filmeService.salvar(filme);
        return "redirect:/";
    }
    

@GetMapping("/detalhes/{id}")
public String detalhes(@PathVariable Long id, Model model) {

    Filme filme = filmeService.buscarPorId(id);

    if (filme == null) {
        return "redirect:/";
    }

    model.addAttribute("filme", filme);

    return "detalhes";
}


@GetMapping("/editar/{id}")
public String editar(@PathVariable Long id, Model model) {

    Filme filme = filmeService.buscarPorId(id);

    if (filme == null) {
        return "redirect:/";
    }

    model.addAttribute("filme", filme);

    return "editar";
}

@PostMapping("/atualizar")
public String atualizar(@ModelAttribute Filme filme) {

    filmeService.salvar(filme);

    return "redirect:/";
}
@GetMapping("/excluir/{id}")
public String excluir(@PathVariable Long id) {

    filmeService.excluir(id);

    return "redirect:/";
}

@PostMapping("/importar")
public String importarFilmes(
        @RequestParam int quantidade,
        Model model) {

    if (quantidade != 20 && quantidade != 50
            && quantidade != 100 && quantidade != 200) {
        return "redirect:/";
    }

    int total = tmdbImportService.importarFilmes(quantidade);

    return "redirect:/?importados=" + total;
}

@PostMapping("/atualizar-trailers")
public String atualizarTrailers() {

    int total = tmdbImportService.atualizarTrailersExistentes();

    return "redirect:/?trailersAtualizados=" + total;
}

@PostMapping("/excluir-todos")
public String excluirTodos() {

    filmeService.excluirTodos();

    return "redirect:/";
}

}