package lk.ac.kln.unimart.review.dto;

public record ReviewCreateRequest(
    Long orderId,
    Long listingId,
    Long revieweeId,
    Integer rating,
    String comment
) {}
