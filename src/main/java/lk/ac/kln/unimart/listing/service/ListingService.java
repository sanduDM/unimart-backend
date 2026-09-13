package lk.ac.kln.unimart.listing.service;

import jakarta.persistence.EntityManager;
import lk.ac.kln.unimart.auth.entity.User;
import lk.ac.kln.unimart.auth.repository.UserRepository;
import lk.ac.kln.unimart.common.exception.ConflictException;
import lk.ac.kln.unimart.common.exception.ForbiddenException;
import lk.ac.kln.unimart.common.exception.ResourceNotFoundException;
import lk.ac.kln.unimart.listing.dto.ListingRequest;
import lk.ac.kln.unimart.listing.entity.Category;
import lk.ac.kln.unimart.listing.entity.Listing;
import lk.ac.kln.unimart.listing.entity.ListingStatus;
import lk.ac.kln.unimart.listing.repository.CategoryRepository;
import lk.ac.kln.unimart.listing.repository.ListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ListingService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ListingRepository listings;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final EntityManager entityManager;

    public ListingService(ListingRepository listings,
                           CategoryRepository categories,
                           UserRepository users,
                           EntityManager entityManager) {
        this.listings = listings;
        this.categories = categories;
        this.users = users;
        this.entityManager = entityManager;
    }

    @Transactional
    public Listing create(ListingRequest request, String email) {
        User seller = users.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categories.findById(request.categoryId().intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(request.title().trim());
        listing.setDescription(request.description().trim());
        listing.setPrice(request.price());
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setCreatedAt(Instant.now());
        listing.setUpdatedAt(Instant.now());

        Listing saved = listings.saveAndFlush(listing);
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Listing get(Long id) {
        return listings.findByIdAndStatusNot(id, ListingStatus.ARCHIVED)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    @Transactional(readOnly = true)
    public Page<Listing> list(String q, Integer categoryId, String status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);

        Specification<Listing> spec = Specification.where(notArchivedByDefault(status));

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), like));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (status != null && !status.isBlank()) {
            ListingStatus parsed = ListingStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsed));
        }

        return listings.findAll(spec, pageable);
    }

    private Specification<Listing> notArchivedByDefault(String status) {
        if (status != null && !status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.notEqual(root.get("status"), ListingStatus.ARCHIVED);
    }

    @Transactional
    public Listing update(Long id, ListingRequest request, String email) {
        Listing listing = requireOwnedListing(id, email);

        Category category = categories.findById(request.categoryId().intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        listing.setTitle(request.title().trim());
        listing.setDescription(request.description().trim());
        listing.setPrice(request.price());
        listing.setCategory(category);
        listing.setUpdatedAt(Instant.now());

        Listing saved = listings.saveAndFlush(listing);
        entityManager.refresh(saved);
        return saved;
    }

    @Transactional
    public void archive(Long id, String email) {
        Listing listing = requireOwnedListing(id, email);

        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new ConflictException("Sold listings cannot be deleted");
        }

        listing.setStatus(ListingStatus.ARCHIVED);
        listing.setUpdatedAt(Instant.now());
        listings.saveAndFlush(listing);
    }

    private Listing requireOwnedListing(Long id, String email) {
        Listing listing = listings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getSeller().getUniversityEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You do not own this listing");
        }

        return listing;
    }
}