package com.example.coalawebbackend.api.commentlike.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentLikeResponse {

    private boolean liked;
    private long likeCount;

    public static CommentLikeResponse of(boolean liked, long likeCount) {
        return new CommentLikeResponse(liked, likeCount);
    }
}
