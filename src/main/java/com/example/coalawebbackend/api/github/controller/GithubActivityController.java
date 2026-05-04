package com.example.coalawebbackend.api.github.controller;

import com.example.coalawebbackend.api.github.dto.GithubActivityResponse;
import com.example.coalawebbackend.api.github.facade.GithubActivityFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GithubActivityController {

    private final GithubActivityFacade githubActivityFacade;

    @GetMapping("/public-activity")
    public ResponseEntity<List<GithubActivityResponse>> getPublicActivity(
            @RequestParam String username,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(githubActivityFacade.getPublicActivity(username, limit));
    }
}
