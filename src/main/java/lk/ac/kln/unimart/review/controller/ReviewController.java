package lk.ac.kln.unimart.review.controller;

import jakarta.validation.Valid;
import lk.ac.kln.unimart.review.dto.ReviewCreateRequest;
import lk.ac.kln.unimart.review.dto.ReviewResponse;
import lk.ac.kln.unimart.review.dto.ReviewUpdateRequest;
import lk.ac.kln.unimart.review.entity.Review;
import lk.ac.kln.unimart.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping("/listings/{listingId}/reviews")
    public Page<ReviewResponse> listByListing(
            @PathVariable Long listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listByListing(listingId, page, size)
                .map(this::toResponse);
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> create(
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {
        Review created = service.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            Authentication authentication) {
        return toResponse(service.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName());
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderId(),
                review.getReviewer().getFullName(),
                review.getReviewer().getUniversityEmail(),
                review.getReviewee().getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt() != null ? review.getCreatedAt().toString() : null,
                review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null
        );
    }
}
