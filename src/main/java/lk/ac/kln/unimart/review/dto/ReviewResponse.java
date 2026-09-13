package lk.ac.kln.unimart.review.dto;

public record ReviewResponse(
    Long id,
    Long orderId,
    String reviewerName,
    String reviewerEmail,
    Long revieweeId,
    Integer rating,
    String comment,
    String createdAt,
    String updatedAt
) {}
