package com.cineflix.cineflix.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cineflix.cineflix.model.Filme;
import com.cineflix.cineflix.repository.FilmeRepository;

@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    public Filme salvar(Filme filme) {
        return filmeRepository.save(filme);
    }

    public List<Filme> listarTodos() {
        return filmeRepository.findAll();
    }
    
    public List<Filme> pesquisar(String termo) {
        return filmeRepository
                .findByTituloContainingIgnoreCaseOrDiretorContainingIgnoreCase(
                        termo, termo);
    }

    public Filme buscarPorId(Long id) {
        return filmeRepository.findById(id).orElse(null);
    }

    public void excluir(Long id) {
        filmeRepository.deleteById(id);
    }
    
    public void excluirTodos() {
        filmeRepository.deleteAll();
    }
}