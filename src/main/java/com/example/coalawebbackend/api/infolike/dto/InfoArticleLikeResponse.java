package com.example.coalawebbackend.api.infolike.dto;

public record InfoArticleLikeResponse(
        boolean liked,
        long likeCount
) {
    public static InfoArticleLikeResponse of(boolean liked, long likeCount) {
        return new InfoArticleLikeResponse(liked, likeCount);
    }
}
