package com.example.coalawebbackend.api.services.service;

import com.example.coalawebbackend.api.services.dto.MemberServiceRequest;
import com.example.coalawebbackend.api.services.dto.MemberServiceResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import com.example.coalawebbackend.domain.memberservice.repository.MemberServiceRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceCatalogService {

    private final MemberServiceRepository memberServiceRepository;
    private final PermissionService permissionService;

    public List<MemberServiceResponse> getServices() {
        return memberServiceRepository.findAllByOrderByTitleAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MemberServiceResponse createService(User actor, MemberServiceRequest request) {
        permissionService.assertModerator(actor);
        String id = generateId(request.title());
        MemberService entity = MemberService.builder()
                .id(id)
                .title(request.title())
                .category(request.category())
                .owner("코알라")
                .summary(request.summary())
                .url(normalizeUrl(request.url()))
                .githubUrl(normalizeOptionalUrl(request.githubUrl()))
                .imageUrl(blankToEmpty(request.imageUrl()))
                .tags(request.tags())
                .status("운영중")
                .audience("코알라 부원")
                .visibility("Public")
                .period("운영 중")
                .description(request.summary())
                .features(List.of("서비스 등록 요청", "운영 정보 검토", "서비스 카탈로그 노출"))
                .stack(List.of("React", "Spring Boot", "PostgreSQL"))
                .build();
        return toResponse(memberServiceRepository.save(entity));
    }

    public MemberServiceResponse getService(String id) {
        return memberServiceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public MemberServiceResponse updateService(User actor, String id, MemberServiceRequest request) {
        permissionService.assertModerator(actor);
        MemberService service = getServiceEntity(id);
        service.updateCatalog(
                request.title(),
                request.category(),
                request.summary(),
                normalizeUrl(request.url()),
                request.githubUrl() == null ? service.getGithubUrl() : normalizeOptionalUrl(request.githubUrl()),
                request.imageUrl() == null ? service.getImageUrl() : blankToEmpty(request.imageUrl()),
                request.tags());
        return toResponse(service);
    }

    @Transactional
    public void retireService(User actor, String id) {
        permissionService.assertModerator(actor);
        getServiceEntity(id).retire();
    }

    private MemberService getServiceEntity(String id) {
        return memberServiceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MemberServiceResponse toResponse(MemberService service) {
        return new MemberServiceResponse(
                service.getId(),
                service.getTitle(),
                service.getCategory(),
                service.getOwner(),
                service.getSummary(),
                service.getUrl(),
                displayGithubUrl(service),
                displayImageUrl(service.getImageUrl()),
                service.getTags(),
                service.getStatus(),
                service.getAudience(),
                service.getVisibility(),
                service.getPeriod(),
                service.getDescription(),
                service.getFeatures(),
                service.getStack()
        );
    }

    private String generateId(String title) {
        String base = title == null ? "service" : title.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        String candidate = base.isBlank() ? "service" : base;
        if (!memberServiceRepository.existsById(candidate)) {
            return candidate;
        }
        return candidate + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String normalizeUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://")
                ? url
                : "https://" + url;
    }

    private String normalizeOptionalUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return normalizeUrl(url.trim());
    }

    private String displayGithubUrl(MemberService service) {
        String githubUrl = service.getGithubUrl();
        if (githubUrl == null || githubUrl.isBlank()) {
            return "";
        }
        String generatedDefault = "https://github.com/JBNU-COALA/" + service.getId();
        return generatedDefault.equals(githubUrl) ? "" : githubUrl;
    }

    private String displayImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("images.unsplash.com")) {
            return "";
        }
        return imageUrl;
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}
