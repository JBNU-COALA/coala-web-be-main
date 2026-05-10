package com.example.coalawebbackend.api.site.controller;

import com.example.coalawebbackend.api.site.dto.SiteContentRequest;
import com.example.coalawebbackend.api.site.dto.SiteContentResponse;
import com.example.coalawebbackend.api.site.service.SiteContentService;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/site")
public class SiteContentController {

    private final SiteContentService siteContentService;
    private final UserService userService;

    @GetMapping("/about")
    public ResponseEntity<SiteContentResponse> getAbout() {
        return ResponseEntity.ok(siteContentService.getAbout());
    }

    @PatchMapping("/about")
    public ResponseEntity<SiteContentResponse> updateAbout(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SiteContentRequest request
    ) {
        return ResponseEntity.ok(siteContentService.updateAbout(userService.findById(userId), request));
    }
}
