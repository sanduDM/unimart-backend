package lk.ac.kln.unimart.listing.controller;

import jakarta.validation.Valid;
import lk.ac.kln.unimart.listing.dto.ListingRequest;
import lk.ac.kln.unimart.listing.dto.ListingResponse;
import lk.ac.kln.unimart.listing.entity.Listing;
import lk.ac.kln.unimart.listing.service.ListingService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingService service;

    public ListingController(ListingService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ListingResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(q, categoryId, status, page, size)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ListingResponse get(@PathVariable Long id) {
        return toResponse(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {
        Listing created = service.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ListingRequest request,
                                   Authentication authentication) {
        return toResponse(service.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.archive(id, authentication.getName());
    }

    private ListingResponse toResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getSeller().getId(),
                listing.getSeller().getFullName(),
                listing.getCategory().getCategoryId().longValue(),
                listing.getCategory().getName(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}