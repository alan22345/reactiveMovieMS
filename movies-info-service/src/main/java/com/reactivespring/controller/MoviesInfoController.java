package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    private MoviesInfoService moviesInfoService;

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }
    @GetMapping("/moviesinfo")
    public Flux<MovieInfo> getAllMovieInfo(@RequestParam(value = "year", required = false) Integer year,
                                           @RequestParam(value = "name", required = false) String name){
        if(year != null){
            return moviesInfoService.getMovieByYear(year);
        } else if (name != null) {
            return moviesInfoService.getMovieByName(name);
        }
        return moviesInfoService.getAllMovieInfos();
    }

    @GetMapping("moviesinfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieById(@PathVariable String id){
        return moviesInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "moviesinfo/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovies(){
        return movieInfoSink.asFlux();
    }


    @PostMapping("/moviesinfo")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo){
        return moviesInfoService.addMovieInfo(movieInfo)
                .doOnNext(savedInfo -> movieInfoSink.tryEmitNext(savedInfo));
    }

    @PostMapping("moviesinfo/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo updatedMovieInfo, @PathVariable String id){
        return moviesInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/moviesinfo/{id}")
    public Mono<Void> deleteMovieInfo(@PathVariable String id){
        return moviesInfoService.deleteMovieInfoById(id);
    }
}
