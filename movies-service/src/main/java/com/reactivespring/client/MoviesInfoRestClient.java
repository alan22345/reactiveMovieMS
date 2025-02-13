package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId){

        var url = moviesInfoUrl.concat("/{id}");
        return webClient.get()
                .uri(url,movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo available for the ID : " + movieId,
                                clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoClientException(
                                    responseMsg,clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMsg -> Mono.error(new MoviesInfoServerException(
                                    "Server exception in MoviesInfoService" + responseMsg)));
                })
                .bodyToMono(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }
}
