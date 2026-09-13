package lk.ac.kln.unimart.review.repository;

import lk.ac.kln.unimart.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByListingId(Long listingId, Pageable pageable);
}
