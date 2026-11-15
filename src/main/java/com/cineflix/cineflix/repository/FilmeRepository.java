
package com.cineflix.cineflix.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineflix.cineflix.model.Filme;

public interface FilmeRepository extends JpaRepository<Filme, Long> {

    List<Filme> findByTituloContainingIgnoreCaseOrDiretorContainingIgnoreCase(
            String titulo, String diretor);
    
    boolean existsByTmdbId(Long tmdbId);

}
