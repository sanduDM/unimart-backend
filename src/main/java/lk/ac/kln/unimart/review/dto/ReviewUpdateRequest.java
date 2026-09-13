package lk.ac.kln.unimart.review.dto;

public record ReviewUpdateRequest(
    Integer rating,
    String comment
) {}
