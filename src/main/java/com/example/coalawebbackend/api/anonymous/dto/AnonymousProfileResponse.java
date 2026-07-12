package com.example.coalawebbackend.api.anonymous.dto;

import com.example.coalawebbackend.domain.anonymous.entity.AnonymousProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnonymousProfileResponse {

    private Long boardId;
    private String displayName;

    public static AnonymousProfileResponse from(AnonymousProfile profile) {
        return AnonymousProfileResponse.builder()
                .boardId(profile.getBoard().getBoardId())
                .displayName(profile.getDisplayName())
                .build();
    }
}
