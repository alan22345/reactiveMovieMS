package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    private ReviewReactorRepository reviewReactorRepository;

    public ReviewHandler(ReviewReactorRepository reviewReactorRepository) {
        this.reviewReactorRepository = reviewReactorRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request){
        return request.bodyToMono(Review.class)
                .flatMap(reviewReactorRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        if (movieInfoId.isPresent()){
            var reviewsFlux = reviewReactorRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(reviewsFlux);
        } else {
            var reviewsFlux = reviewReactorRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }

    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactorRepository.findById(reviewId);
        return existingReview.flatMap(review -> request.bodyToMono(Review.class)
                .map(reqReview -> {
                    review.setComment(reqReview.getComment());
                    review.setRating(reqReview.getRating());
                    return review;
                })
                .flatMap(reviewReactorRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
        );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactorRepository.findById(reviewId);

        return existingReview.flatMap(review -> reviewReactorRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }
}
