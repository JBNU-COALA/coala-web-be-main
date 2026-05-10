package com.example.coalawebbackend.api.services.service;

import com.example.coalawebbackend.api.services.dto.MemberServiceRequest;
import com.example.coalawebbackend.api.services.dto.MemberServiceResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import com.example.coalawebbackend.domain.memberservice.repository.MemberServiceRepository;
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

    public List<MemberServiceResponse> getServices() {
        return memberServiceRepository.findAllByOrderByTitleAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MemberServiceResponse createService(MemberServiceRequest request) {
        String id = generateId(request.title());
        MemberService entity = MemberService.builder()
                .id(id)
                .title(request.title())
                .category(request.category())
                .owner("코알라")
                .summary(request.summary())
                .url(normalizeUrl(request.url()))
                .githubUrl(buildGithubUrl(id))
                .imageUrl(defaultImageFor(request.category()))
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

    private MemberServiceResponse toResponse(MemberService service) {
        return new MemberServiceResponse(
                service.getId(),
                service.getTitle(),
                service.getCategory(),
                service.getOwner(),
                service.getSummary(),
                service.getUrl(),
                service.getGithubUrl(),
                service.getImageUrl(),
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

    private String buildGithubUrl(String id) {
        return "https://github.com/JBNU-COALA/" + id;
    }

    private String defaultImageFor(String category) {
        return switch (category == null ? "" : category.toLowerCase()) {
            case "ai" -> "https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=900&q=80";
            case "learning" -> "https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=900&q=80";
            case "community" -> "https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=900&q=80";
            default -> "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=900&q=80";
        };
    }
}
