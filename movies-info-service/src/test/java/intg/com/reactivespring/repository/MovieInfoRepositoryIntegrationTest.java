package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp(){

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll(){
        var moviesInfoFlux = movieInfoRepository.findAll();
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById(){
        var movieMono = movieInfoRepository.findById("abc");
        StepVerifier.create(movieMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo(){
        var movieInfo = new MovieInfo(null, "Big Short",
                        2009, List.of("Christian Bale", "Steve Carell"), LocalDate.parse("2009-06-15"));

        var moviesInfoMono = movieInfoRepository.save(movieInfo);
        StepVerifier.create(moviesInfoMono)
                .assertNext(movie -> {
                    assertEquals("Big Short", movie.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){
        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2023);

        var moviesInfoMono = movieInfoRepository.save(movieInfo);

        StepVerifier.create(moviesInfoMono)
                .assertNext(movie -> {
                    assertEquals(2023, movie.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovie() {
        movieInfoRepository.deleteById("abc").block();
        var moviesInfoFlux = movieInfoRepository.findAll();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    void findMovieByYear(){
        var moviesInfoFlux = movieInfoRepository.findByYear(2005);

        StepVerifier.create(moviesInfoFlux)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();

    }

    @Test
    void findMovieByName(){
        var moviesInfoFlux = movieInfoRepository.findByName("Batman Begins");
        StepVerifier.create(moviesInfoFlux)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Begins", movieInfo.getName());
                }).verifyComplete();
    }
}