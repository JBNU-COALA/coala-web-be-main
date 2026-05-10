package com.example.coalawebbackend.api.site.service;

import com.example.coalawebbackend.api.site.dto.SiteContentRequest;
import com.example.coalawebbackend.api.site.dto.SiteContentResponse;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.site.entity.SiteContent;
import com.example.coalawebbackend.domain.site.repository.SiteContentRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteContentService {

    private static final String ABOUT_KEY = "about";
    private static final SiteContentResponse DEFAULT_ABOUT = new SiteContentResponse(
            "함께 만들고 운영하는 개발 동아리",
            "코알라는 프로젝트, 스터디, 서비스 운영을 통해 개발 경험을 쌓는 전북대학교 개발 동아리입니다.",
            List.of("프로젝트", "스터디", "서비스 운영", "커뮤니티")
    );

    private final SiteContentRepository siteContentRepository;
    private final PermissionService permissionService;

    public SiteContentResponse getAbout() {
        return siteContentRepository.findById(ABOUT_KEY)
                .map(this::toResponse)
                .orElse(DEFAULT_ABOUT);
    }

    @Transactional
    public SiteContentResponse updateAbout(User actor, SiteContentRequest request) {
        permissionService.assertModerator(actor);
        String chips = String.join(",", normalizeChips(request.chips()));
        SiteContent content = siteContentRepository.findById(ABOUT_KEY)
                .orElseGet(() -> SiteContent.builder()
                        .key(ABOUT_KEY)
                        .title(DEFAULT_ABOUT.title())
                        .description(DEFAULT_ABOUT.description())
                        .chips(String.join(",", DEFAULT_ABOUT.chips()))
                        .build());
        content.update(request.title().trim(), request.description().trim(), chips);
        return toResponse(siteContentRepository.save(content));
    }

    private SiteContentResponse toResponse(SiteContent content) {
        return new SiteContentResponse(
                content.getTitle(),
                content.getDescription(),
                parseChips(content.getChips())
        );
    }

    private List<String> normalizeChips(List<String> chips) {
        if (chips == null || chips.isEmpty()) {
            return DEFAULT_ABOUT.chips();
        }
        List<String> normalized = chips.stream()
                .map(String::trim)
                .filter(chip -> !chip.isBlank())
                .distinct()
                .limit(8)
                .toList();
        return normalized.isEmpty() ? DEFAULT_ABOUT.chips() : normalized;
    }

    private List<String> parseChips(String chips) {
        if (chips == null || chips.isBlank()) {
            return DEFAULT_ABOUT.chips();
        }
        return Arrays.stream(chips.split(","))
                .map(String::trim)
                .filter(chip -> !chip.isBlank())
                .toList();
    }
}
