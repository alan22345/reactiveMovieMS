package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactorRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    @MockBean
    private ReviewReactorRepository reviewReactorRepository;

    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";
    @Test
    void addReview(){
        var review = new Review("abc", 1L, "Great movie", 9.0);

        when(reviewReactorRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(review));

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void getReviews(){
        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewReactorRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReview() {
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);

        when(reviewReactorRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Not an Awesome Movie", 8.0)));
        when(reviewReactorRepository.findById((String) any()))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));

        webTestClient
                .put()
                .uri("/v1/reviews/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse ->{
                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : "+ updatedReview);
                    assertEquals(8.0,updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });

    }

    @Test
    void deleteReview() {
        var reviewId= "abc";
        when(reviewReactorRepository.findById((String) any())).thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
        when(reviewReactorRepository.deleteById((String) any())).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/v1/reviews/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void addReview_validation(){
        var review = new Review(null, null, "Great movie", -9.0);

        when(reviewReactorRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(review));

        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }
}
