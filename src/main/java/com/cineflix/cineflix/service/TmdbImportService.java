package com.cineflix.cineflix.service;

import org.springframework.stereotype.Service;
import com.cineflix.cineflix.integration.TmdbService;
import com.cineflix.cineflix.repository.FilmeRepository;

import com.cineflix.cineflix.model.Filme;
import tools.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;

@Service
public class TmdbImportService {

    private final TmdbService tmdbService;
    private final FilmeRepository filmeRepository;

    public TmdbImportService(
            TmdbService tmdbService,
            FilmeRepository filmeRepository) {

        this.tmdbService = tmdbService;
        this.filmeRepository = filmeRepository;
    }
    
    private Filme converterParaFilme(JsonNode dados) {

        Filme filme = new Filme();

        // Identificação do filme no TMDB
        filme.setTmdbId(dados.path("id").asLong());
        
        filme.setTrailerKey(
        	    tmdbService.buscarTrailer(filme.getTmdbId())
        	);

        // Título
        String titulo = dados.path("title").asText();

        if (titulo.isBlank()) {
            titulo = dados.path("original_title").asText();
        }

        filme.setTitulo(titulo);

        // Sinopse
        filme.setSinopse(dados.path("overview").asText());

        // Diretor
        String diretor = "Não informado";

        for (JsonNode pessoa : dados.path("credits").path("crew")) {

            if ("Director".equals(pessoa.path("job").asText())) {
                diretor = pessoa.path("name").asText();
                break;
            }
        }

        filme.setDiretor(diretor);

        // Ano de lançamento
        String data = dados.path("release_date").asText();

        if (data.length() >= 4) {
            filme.setAnoLancamento(
                    Integer.parseInt(data.substring(0, 4)));
        }

        // Duração
        if (!dados.path("runtime").isMissingNode()
                && !dados.path("runtime").isNull()) {

            filme.setDuracao(dados.path("runtime").asInt());
        }

        // Gêneros
        StringBuilder generos = new StringBuilder();

        for (JsonNode genero : dados.path("genres")) {

            if (generos.length() > 0) {
                generos.append(", ");
            }

            generos.append(genero.path("name").asText());
        }

        filme.setGenero(generos.toString());

        // Pôster
        filme.setPosterPath(
                dados.path("poster_path").asText());

        return filme;
    }
    
    private int importarLista(
            boolean populares,
            int quantidade,
            Set<Long> idsImportados) {

        int importados = 0;
        int pagina = 1;

        while (importados < quantidade) {

            JsonNode resposta;

            if (populares) {
                resposta = tmdbService.buscarFilmesPopulares(pagina);
            } else {
                resposta = tmdbService.buscarFilmesMaisBemAvaliados(pagina);
            }

            JsonNode resultados = resposta.path("results");

            if (!resultados.isArray() || resultados.isEmpty()) {
                break;
            }

            for (JsonNode item : resultados) {

                if (importados >= quantidade) {
                    break;
                }

                Long tmdbId = item.path("id").asLong();

                if (tmdbId <= 0
                        || idsImportados.contains(tmdbId)
                        || filmeRepository.existsByTmdbId(tmdbId)) {
                    continue;
                }

                JsonNode detalhes = tmdbService.buscarDetalhesFilme(tmdbId);

                Filme filme = converterParaFilme(detalhes);

                filmeRepository.save(filme);

                idsImportados.add(tmdbId);
                importados++;
            }

            pagina++;

            if (pagina > resposta.path("total_pages").asInt()) {
                break;
            }
        }

        return importados;
    }
    
    public int atualizarTrailersExistentes() {

        int atualizados = 0;

        for (Filme filme : filmeRepository.findAll()) {

            // Ignora filmes que não vieram do TMDB
            if (filme.getTmdbId() == null) {
                continue;
            }

            // Ignora filmes que já possuem trailer
            if (filme.getTrailerKey() != null
                    && !filme.getTrailerKey().isBlank()) {
                continue;
            }

            // Busca o trailer no TMDB
            String trailer = tmdbService.buscarTrailer(filme.getTmdbId());

            // Salva somente quando encontrar um trailer
            if (trailer != null && !trailer.isBlank()) {

                filme.setTrailerKey(trailer);

                filmeRepository.save(filme);

                atualizados++;
            }
        }

        return atualizados;
    }
    
    public int importarFilmes(int quantidade) {

        Set<Long> idsImportados = new HashSet<>();

        int populares = (quantidade + 1) / 2;
        int maisBemAvaliados = quantidade / 2;

        int totalImportado = 0;

        totalImportado += importarLista(
                true, populares, idsImportados);

        totalImportado += importarLista(
                false, maisBemAvaliados, idsImportados);

        return totalImportado;
    }

}
