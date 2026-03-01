package com.example.coalawebbackend.api.postlike.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostLikeResponse {

    private boolean liked;
    private long likeCount;

    public static PostLikeResponse of(boolean liked, long likeCount) {
        return new PostLikeResponse(liked, likeCount);
    }
}
