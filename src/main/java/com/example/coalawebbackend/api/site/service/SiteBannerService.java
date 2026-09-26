package com.example.coalawebbackend.api.site.service;

import com.example.coalawebbackend.api.site.dto.SiteBannerRequest;
import com.example.coalawebbackend.api.site.dto.SiteBannerResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.site.entity.SiteBanner;
import com.example.coalawebbackend.domain.site.repository.SiteBannerRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteBannerService {
    private final SiteBannerRepository banners;
    private final PermissionService permissions;

    public List<SiteBannerResponse> publicBanners() {
        return banners.findByEnabledTrueOrderBySortOrderAscIdAsc().stream().map(this::response).toList();
    }

    public List<SiteBannerResponse> adminBanners(User actor) {
        permissions.assertModerator(actor);
        return banners.findAllByOrderBySortOrderAscIdAsc().stream().map(this::response).toList();
    }

    @Transactional
    public SiteBannerResponse create(User actor, SiteBannerRequest request) {
        permissions.assertModerator(actor);
        SiteBanner banner = new SiteBanner();
        update(banner, request);
        return response(banners.save(banner));
    }

    @Transactional
    public SiteBannerResponse update(User actor, Long id, SiteBannerRequest request) {
        permissions.assertModerator(actor);
        SiteBanner banner = find(id);
        update(banner, request);
        return response(banner);
    }

    @Transactional
    public void delete(User actor, Long id) {
        permissions.assertModerator(actor);
        banners.delete(find(id));
    }

    private SiteBanner find(Long id) {
        return banners.findById(id).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void update(SiteBanner banner, SiteBannerRequest request) {
        banner.update(request.title().trim(), text(request.eyebrow()), text(request.description()),
                text(request.imageUrl()), request.targetPath(), request.actionLabel().trim(),
                request.tone(), request.sortOrder(), request.enabled());
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private SiteBannerResponse response(SiteBanner banner) {
        return new SiteBannerResponse(banner.getId(), banner.getTitle(), banner.getEyebrow(),
                banner.getDescription(), banner.getImageUrl(), banner.getTargetPath(),
                banner.getActionLabel(), banner.getTone(), banner.getSortOrder(), banner.isEnabled());
    }
}
