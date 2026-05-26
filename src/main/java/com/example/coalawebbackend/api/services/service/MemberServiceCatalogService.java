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

    public List<MemberServiceResponse> getServices(User actor) {
        return memberServiceRepository.findAllByOrderByTitleAsc()
                .stream()
                .map(service -> toResponse(service, actor))
                .toList();
    }

    @Transactional
    public MemberServiceResponse createService(User actor, MemberServiceRequest request) {
        String id = generateId(request.title());
        MemberService entity = MemberService.builder()
                .id(id)
                .title(request.title())
                .category(request.category())
                .owner(defaultIfBlank(request.owner(), displayName(actor)))
                .ownerUser(actor)
                .summary(request.summary())
                .url(normalizeUrl(request.url()))
                .githubUrl(normalizeOptionalUrl(request.githubUrl()))
                .imageUrl(blankToEmpty(request.imageUrl()))
                .additionalImageUrls(normalizeImageUrls(request.additionalImageUrls()))
                .tags(request.tags())
                .status(normalizeStatus(request.status()))
                .audience("코알라 부원")
                .visibility("Public")
                .period(periodForStatus(normalizeStatus(request.status())))
                .description(request.summary())
                .features(List.of("서비스 등록 요청", "운영 정보 검토", "서비스 카탈로그 노출"))
                .stack(List.of("React", "Spring Boot", "PostgreSQL"))
                .build();
        return toResponse(memberServiceRepository.save(entity), actor);
    }

    public MemberServiceResponse getService(String id, User actor) {
        return memberServiceRepository.findById(id)
                .map(service -> toResponse(service, actor))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public MemberServiceResponse updateService(User actor, String id, MemberServiceRequest request) {
        MemberService service = getServiceEntity(id);
        assertCanManageService(actor, service);
        service.updateCatalog(
                request.title(),
                request.category(),
                defaultIfBlank(request.owner(), service.getOwner()),
                request.summary(),
                normalizeUrl(request.url()),
                request.githubUrl() == null ? service.getGithubUrl() : normalizeOptionalUrl(request.githubUrl()),
                request.imageUrl() == null ? service.getImageUrl() : blankToEmpty(request.imageUrl()),
                request.additionalImageUrls() == null
                        ? service.getAdditionalImageUrls()
                        : normalizeImageUrls(request.additionalImageUrls()),
                request.tags(),
                normalizeStatus(defaultIfBlank(request.status(), service.getStatus())));
        return toResponse(service, actor);
    }

    @Transactional
    public void retireService(User actor, String id) {
        MemberService service = getServiceEntity(id);
        assertCanManageService(actor, service);
        service.retire();
    }

    private MemberService getServiceEntity(String id) {
        return memberServiceRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private MemberServiceResponse toResponse(MemberService service, User actor) {
        String status = normalizeStatus(service.getStatus());
        return new MemberServiceResponse(
                service.getId(),
                service.getTitle(),
                service.getCategory(),
                service.getOwner(),
                service.getSummary(),
                service.getUrl(),
                displayGithubUrl(service),
                displayImageUrl(service.getImageUrl()),
                service.getAdditionalImageUrls().stream()
                        .map(this::displayImageUrl)
                        .filter(value -> !value.isBlank())
                        .limit(5)
                        .toList(),
                service.getTags(),
                status,
                service.getAudience(),
                service.getVisibility(),
                periodForStatus(status),
                service.getDescription(),
                service.getFeatures(),
                service.getStack(),
                canManageService(actor, service)
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

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        return imageUrls.stream()
                .map(this::blankToEmpty)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(5)
                .toList();
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String displayName(User user) {
        return user.getNickname() == null || user.getNickname().isBlank() ? user.getName() : user.getNickname();
    }

    private void assertCanManageService(User actor, MemberService service) {
        if (!canManageService(actor, service)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean canManageService(User actor, MemberService service) {
        boolean isOwner = actor != null
                && service.getOwnerUser() != null
                && actor.getId().equals(service.getOwnerUser().getId());
        return isOwner || permissionService.canModerate(actor);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "운영중";
        }
        String trimmed = status.trim();
        if ("운영종료".equals(trimmed)) {
            return "운영완료";
        }
        if ("운영완료".equals(trimmed) || "운영중지".equals(trimmed) || "운영중".equals(trimmed)) {
            return trimmed;
        }
        return "운영중";
    }

    private String periodForStatus(String status) {
        if ("운영중지".equals(status)) {
            return "운영 중지";
        }
        if ("운영완료".equals(status)) {
            return "운영 완료";
        }
        return "운영 중";
    }
}
