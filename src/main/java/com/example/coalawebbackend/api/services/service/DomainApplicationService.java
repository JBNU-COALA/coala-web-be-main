package com.example.coalawebbackend.api.services.service;

import com.example.coalawebbackend.api.services.dto.DomainApplicationRequest;
import com.example.coalawebbackend.api.services.dto.DomainApplicationResponse;
import com.example.coalawebbackend.api.services.dto.DomainApplicationUpdateRequest;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.instance.entity.DomainApplication;
import com.example.coalawebbackend.domain.instance.repository.DomainApplicationRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainApplicationService {

    private static final String DOMAIN_PREFIX = "coala.jbnu.ac.kr/services/";

    private final DomainApplicationRepository domainApplicationRepository;
    private final PermissionService permissionService;

    public List<DomainApplicationResponse> getApplications(User actor) {
        List<DomainApplication> applications = permissionService.canModerate(actor)
                ? domainApplicationRepository.findAllByOrderByRequestedAtDesc()
                : domainApplicationRepository.findByUser_IdOrderByRequestedAtDesc(actor.getId());
        return applications.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DomainApplicationResponse createApplication(User actor, DomainApplicationRequest request) {
        String desiredAddress = request.desiredAddress().trim().toLowerCase();
        if (domainApplicationRepository.existsByDesiredAddressIgnoreCase(desiredAddress)) {
            throw new CustomException(ErrorCode.DOMAIN_ALREADY_EXISTS);
        }
        DomainApplication application = DomainApplication.builder()
                .id(nextApplicationId())
                .user(actor)
                .applicantName(defaultIfBlank(request.applicantName(), actor.getName()))
                .studentId(defaultIfBlank(request.studentId(), actor.getStudentId()))
                .contactEmail(defaultIfBlank(request.contactEmail(), actor.getEmail()))
                .serviceName(request.serviceName().trim())
                .desiredAddress(desiredAddress)
                .requestedDomain(DOMAIN_PREFIX + desiredAddress)
                .repositoryUrl(normalizeUrl(request.repositoryUrl().trim()))
                .targetUrl(normalizeOptionalUrl(request.targetUrl()))
                .purpose(request.purpose().trim())
                .requestedAt(LocalDate.now())
                .status("pending")
                .build();
        return toResponse(domainApplicationRepository.save(application));
    }

    @Transactional
    public DomainApplicationResponse updateApplication(User actor, String applicationId, DomainApplicationUpdateRequest request) {
        permissionService.assertModerator(actor);
        DomainApplication application = getApplicationEntity(applicationId);
        application.update(request.status(), request.adminNote());
        return toResponse(application);
    }

    private DomainApplication getApplicationEntity(String applicationId) {
        return domainApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private DomainApplicationResponse toResponse(DomainApplication application) {
        return new DomainApplicationResponse(
                application.getId(),
                application.getUser() == null ? null : application.getUser().getId(),
                application.getApplicantName(),
                application.getStudentId(),
                application.getContactEmail(),
                application.getServiceName(),
                application.getDesiredAddress(),
                application.getRequestedDomain(),
                application.getRepositoryUrl(),
                application.getTargetUrl(),
                application.getPurpose(),
                application.getRequestedAt().toString(),
                application.getProcessedAt() == null ? null : application.getProcessedAt().toString(),
                application.getStatus(),
                application.getAdminNote()
        );
    }

    private String nextApplicationId() {
        return "dom-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String normalizeUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://")
                ? url
                : "https://" + url;
    }

    private String normalizeOptionalUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        return normalizeUrl(url.trim());
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
