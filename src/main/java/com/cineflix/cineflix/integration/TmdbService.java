
package com.cineflix.cineflix.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(
            @Value("${tmdb.api.url}") String apiUrl,
            @Value("${tmdb.api.token}") String apiToken) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiToken)
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public JsonNode buscarFilmesPopulares(int pagina) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("language", "pt-BR")
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }
    
    public JsonNode buscarFilmesMaisBemAvaliados(int pagina) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/top_rated")
                        .queryParam("language", "pt-BR")
                        .queryParam("page", pagina)
                        .build())
                .retrieve()
                .body(JsonNode.class);
    }
    
    public JsonNode buscarDetalhesFilme(Long tmdbId) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .queryParam("language", "pt-BR")
                        .queryParam("append_to_response", "credits")
                        .build(tmdbId))
                .retrieve()
                .body(JsonNode.class);
    }
    
    public String buscarTrailer(Long tmdbId) {

        JsonNode resposta = restClient.get()
                .uri("/movie/" + tmdbId + "/videos?language=pt-BR")
                .retrieve()
                .body(JsonNode.class);

        if (resposta == null || !resposta.has("results")) {
            return null;
        }

        JsonNode videos = resposta.path("results");

        // Primeiro, procura um trailer oficial do YouTube
        for (JsonNode video : videos) {

            if ("YouTube".equals(video.path("site").asText())
                    && "Trailer".equals(video.path("type").asText())
                    && video.path("official").asBoolean()) {

                return video.path("key").asText();
            }
        }

        // Se não encontrar um oficial, procura qualquer trailer do YouTube
        for (JsonNode video : videos) {

            if ("YouTube".equals(video.path("site").asText())
                    && "Trailer".equals(video.path("type").asText())) {

                return video.path("key").asText();
            }
        }

        return null;
    }
}
