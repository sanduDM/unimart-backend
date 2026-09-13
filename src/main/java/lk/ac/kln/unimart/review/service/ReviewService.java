package lk.ac.kln.unimart.review.service;

import jakarta.persistence.EntityManager;
import lk.ac.kln.unimart.auth.entity.User;
import lk.ac.kln.unimart.auth.repository.UserRepository;
import lk.ac.kln.unimart.common.exception.ForbiddenException;
import lk.ac.kln.unimart.common.exception.ResourceNotFoundException;
import lk.ac.kln.unimart.listing.entity.Listing;
import lk.ac.kln.unimart.listing.repository.ListingRepository;
import lk.ac.kln.unimart.review.dto.ReviewCreateRequest;
import lk.ac.kln.unimart.review.dto.ReviewUpdateRequest;
import lk.ac.kln.unimart.review.entity.Review;
import lk.ac.kln.unimart.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ReviewRepository reviews;
    private final ListingRepository listings;
    private final UserRepository users;
    private final EntityManager entityManager;

    public ReviewService(ReviewRepository reviews,
                          ListingRepository listings,
                          UserRepository users,
                          EntityManager entityManager) {
        this.reviews = reviews;
        this.listings = listings;
        this.users = users;
        this.entityManager = entityManager;
    }

    @Transactional
    public Review create(ReviewCreateRequest request, String email) {
        User reviewer = users.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listings.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        User reviewee = users.findById(request.revieweeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewee not found"));

        Review review = new Review();
        review.setOrderId(request.orderId());
        review.setListing(listing);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviews.saveAndFlush(review);
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Review> listByListing(Long listingId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);
        return reviews.findByListingId(listingId, pageable);
    }

    @Transactional
    public Review update(Long id, ReviewUpdateRequest request, String email) {
        Review review = requireOwnedReview(id, email);

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviews.saveAndFlush(review);
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional
    public void delete(Long id, String email) {
        Review review = requireOwnedReview(id, email);
        reviews.delete(review);
    }

    private Review requireOwnedReview(Long id, String email) {
        Review review = reviews.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getReviewer().getUniversityEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You do not own this review");
        }

        return review;
    }
}
