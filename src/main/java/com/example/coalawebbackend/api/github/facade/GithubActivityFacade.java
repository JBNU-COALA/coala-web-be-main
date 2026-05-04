package com.example.coalawebbackend.api.github.facade;

import com.example.coalawebbackend.api.github.dto.GithubActivityResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GithubActivityFacade {

    private static final Pattern GITHUB_USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$");
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final RestClient.Builder restClientBuilder;
    private final Map<String, CachedActivity> activityCache = new ConcurrentHashMap<>();

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Value("${github.api.token:}")
    private String githubApiToken;

    public List<GithubActivityResponse> getPublicActivity(String username, int limit) {
        if (username == null || !GITHUB_USERNAME_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid GitHub username");
        }

        int perPage = Math.max(1, Math.min(limit, 30));
        String cacheKey = username.toLowerCase() + ":" + perPage;
        CachedActivity cachedActivity = activityCache.get(cacheKey);

        if (cachedActivity != null && cachedActivity.isFresh()) {
            return cachedActivity.responses();
        }

        List<GithubActivityResponse> responses = fetchPublicActivity(username, perPage);
        activityCache.put(cacheKey, new CachedActivity(responses, Instant.now()));

        return responses;
    }

    private List<GithubActivityResponse> fetchPublicActivity(String username, int perPage) {
        try {
            RestClient client = restClientBuilder
                    .baseUrl(githubApiBaseUrl)
                    .defaultHeader("Accept", "application/vnd.github+json")
                    .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build();

            RestClient.RequestHeadersSpec<?> request = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{username}/events/public")
                            .queryParam("per_page", perPage)
                            .build(username));

            if (githubApiToken != null && !githubApiToken.isBlank()) {
                request.headers(headers -> headers.setBearerAuth(githubApiToken));
            }

            JsonNode events = request.retrieve().body(JsonNode.class);
            if (events == null || !events.isArray()) {
                return List.of();
            }

            List<GithubActivityResponse> responses = new ArrayList<>();
            events.forEach(event -> responses.add(toResponse(event)));

            return responses;
        } catch (RestClientResponseException exception) {
            return List.of();
        } catch (RestClientException exception) {
            return List.of();
        }
    }

    private GithubActivityResponse toResponse(JsonNode event) {
        String githubType = event.path("type").asText("Event");
        String repository = event.path("repo").path("name").asText("-");
        String actor = event.path("actor").path("login").asText("");
        String createdAt = event.path("created_at").asText("");

        return GithubActivityResponse.builder()
                .id(event.path("id").asText())
                .type(mapType(githubType))
                .title(buildTitle(githubType, event))
                .repository(repository)
                .description(buildDescription(githubType, event))
                .timeLabel(buildTimeLabel(createdAt))
                .url(buildUrl(repository, actor))
                .actor(actor)
                .createdAt(createdAt)
                .build();
    }

    private String mapType(String githubType) {
        return switch (githubType) {
            case "PushEvent" -> "commit";
            case "PullRequestEvent" -> "pull-request";
            case "ReleaseEvent" -> "release";
            default -> "note";
        };
    }

    private String buildTitle(String githubType, JsonNode event) {
        JsonNode payload = event.path("payload");

        return switch (githubType) {
            case "PushEvent" -> buildPushTitle(payload);
            case "PullRequestEvent" -> {
                String action = payload.path("action").asText("updated");
                String title = payload.path("pull_request").path("title").asText("Pull request");
                yield action + ": " + title;
            }
            case "ReleaseEvent" -> "Release " + payload.path("release").path("tag_name").asText("");
            case "IssuesEvent" -> "Issue " + payload.path("issue").path("title").asText("");
            case "CreateEvent" -> "Created " + payload.path("ref_type").asText("resource");
            case "ForkEvent" -> "Forked repository";
            case "WatchEvent" -> "Starred repository";
            default -> githubType;
        };
    }

    private String buildPushTitle(JsonNode payload) {
        JsonNode commits = payload.path("commits");
        if (commits.isArray() && !commits.isEmpty()) {
            return commits.get(0).path("message").asText("Push");
        }

        return "Push";
    }

    private String buildDescription(String githubType, JsonNode event) {
        JsonNode payload = event.path("payload");
        String repository = event.path("repo").path("name").asText("-");

        return switch (githubType) {
            case "PushEvent" -> {
                int count = payload.path("commits").isArray() ? payload.path("commits").size() : 0;
                yield count + " commits pushed to " + repository;
            }
            case "PullRequestEvent" -> payload.path("pull_request").path("html_url").asText(repository);
            case "ReleaseEvent" -> payload.path("release").path("html_url").asText(repository);
            default -> repository;
        };
    }

    private String buildTimeLabel(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) {
            return "GitHub";
        }

        try {
            Instant created = Instant.parse(createdAt);
            long days = Duration.between(created, Instant.now()).toDays();

            if (days == 0) {
                return "\uC624\uB298";
            }
            if (days == 1) {
                return "\uC5B4\uC81C";
            }
            return days + "\uC77C \uC804";
        } catch (RuntimeException exception) {
            return createdAt;
        }
    }

    private String buildUrl(String repository, String actor) {
        if (!repository.isBlank() && !repository.equals("-")) {
            return "https://github.com/" + repository;
        }

        return "https://github.com/" + actor;
    }

    private record CachedActivity(List<GithubActivityResponse> responses, Instant cachedAt) {

        private boolean isFresh() {
            return cachedAt.plus(CACHE_TTL).isAfter(Instant.now());
        }
    }
}
