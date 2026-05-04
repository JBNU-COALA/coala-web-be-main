package com.example.coalawebbackend.api.github.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubActivityResponse {

    private String id;
    private String type;
    private String title;
    private String repository;
    private String description;
    private String timeLabel;
    private String url;
    private String actor;
    private String createdAt;
}
