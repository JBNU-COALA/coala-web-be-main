package com.example.coalawebbackend.api.admin.controller;

import com.example.coalawebbackend.api.site.dto.SiteBannerRequest;
import com.example.coalawebbackend.api.site.dto.SiteBannerResponse;
import com.example.coalawebbackend.api.site.service.SiteBannerService;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/banners")
public class AdminBannerController {
    private final SiteBannerService banners;
    private final UserService users;

    @GetMapping
    public List<SiteBannerResponse> list(@AuthenticationPrincipal String userId) {
        return banners.adminBanners(users.findById(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiteBannerResponse create(@AuthenticationPrincipal String userId,
                                     @Valid @RequestBody SiteBannerRequest request) {
        return banners.create(users.findById(userId), request);
    }

    @PatchMapping("/{id}")
    public SiteBannerResponse update(@AuthenticationPrincipal String userId, @PathVariable Long id,
                                     @Valid @RequestBody SiteBannerRequest request) {
        return banners.update(users.findById(userId), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal String userId, @PathVariable Long id) {
        banners.delete(users.findById(userId), id);
    }
}
