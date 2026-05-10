package com.example.coalawebbackend.api.services.service;

import com.example.coalawebbackend.api.services.dto.InstanceApplicationRequest;
import com.example.coalawebbackend.api.services.dto.InstanceApplicationResponse;
import com.example.coalawebbackend.api.services.dto.InstanceApplicationUpdateRequest;
import com.example.coalawebbackend.api.services.dto.ServiceInquiryRequest;
import com.example.coalawebbackend.api.services.dto.ServiceInquiryResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceAttachedFile;
import com.example.coalawebbackend.domain.instance.entity.InstanceSpec;
import com.example.coalawebbackend.domain.instance.entity.ServiceInquiry;
import com.example.coalawebbackend.domain.instance.repository.InstanceApplicationRepository;
import com.example.coalawebbackend.domain.instance.repository.ServiceInquiryRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstanceApplicationService {

    private static final DateTimeFormatter INQUIRY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final InstanceApplicationRepository instanceApplicationRepository;
    private final ServiceInquiryRepository serviceInquiryRepository;
    private final PermissionService permissionService;

    public List<InstanceApplicationResponse> getApplications(User actor) {
        permissionService.assertModerator(actor);
        return instanceApplicationRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public InstanceApplicationResponse createApplication(InstanceApplicationRequest request) {
        InstanceApplication application = InstanceApplication.builder()
                .id(nextApplicationId())
                .applicantName(defaultIfBlank(request.applicantName(), "코알라 신청자"))
                .studentId(defaultIfBlank(request.studentId(), "00000000"))
                .keyEmail(defaultIfBlank(request.keyEmail(), "coala.member@example.com"))
                .instanceType(request.instanceType())
                .purpose(request.purpose())
                .duration(request.duration())
                .requestedAt(LocalDate.now())
                .status("pending")
                .attachedFiles(List.of())
                .specs(InstanceSpec.forType(request.instanceType()))
                .build();
        return toApplicationResponse(instanceApplicationRepository.save(application));
    }

    @Transactional
    public InstanceApplicationResponse updateApplication(
            User actor,
            String applicationId,
            InstanceApplicationUpdateRequest request
    ) {
        permissionService.assertModerator(actor);
        InstanceApplication application = getApplicationEntity(applicationId);
        application.update(request.instanceType(), request.duration(), request.purpose(), request.status(), request.adminNote());
        return toApplicationResponse(application);
    }

    public InstanceApplicationResponse getApplication(User actor, String applicationId) {
        permissionService.assertModerator(actor);
        return toApplicationResponse(getApplicationEntity(applicationId));
    }

    public List<ServiceInquiryResponse> getInquiries(User actor) {
        permissionService.assertModerator(actor);
        return serviceInquiryRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(this::toInquiryResponse)
                .toList();
    }

    @Transactional
    public ServiceInquiryResponse createInquiry(ServiceInquiryRequest request) {
        String content = request.content().trim();
        ServiceInquiry inquiry = ServiceInquiry.builder()
                .id(nextInquiryId())
                .title(request.title().trim())
                .summary(content.length() > 100 ? content.substring(0, 100) : content)
                .content(content)
                .author(defaultIfBlank(request.author(), "코알라"))
                .createdDate(LocalDate.now())
                .status("검토 중")
                .statusClass("status--pending")
                .build();
        return toInquiryResponse(serviceInquiryRepository.save(inquiry));
    }

    private InstanceApplication getApplicationEntity(String applicationId) {
        return instanceApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private InstanceApplicationResponse toApplicationResponse(InstanceApplication application) {
        return new InstanceApplicationResponse(
                application.getId(),
                application.getApplicantName(),
                application.getStudentId(),
                application.getKeyEmail(),
                application.getInstanceType(),
                application.getPurpose(),
                application.getDuration(),
                application.getRequestedAt().toString(),
                application.getApprovedAt() == null ? null : application.getApprovedAt().toString(),
                application.getStatus(),
                application.getAdminNote(),
                application.getAttachedFiles().stream().map(this::toFileResponse).toList(),
                new InstanceApplicationResponse.InstanceSpecResponse(
                        application.getSpecs().getCpu(),
                        application.getSpecs().getRam(),
                        application.getSpecs().getDisk()
                )
        );
    }

    private InstanceApplicationResponse.AttachedFileResponse toFileResponse(InstanceAttachedFile file) {
        return new InstanceApplicationResponse.AttachedFileResponse(
                file.getName(),
                file.getSize(),
                file.getUploadedAt()
        );
    }

    private ServiceInquiryResponse toInquiryResponse(ServiceInquiry inquiry) {
        return new ServiceInquiryResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getSummary(),
                inquiry.getAuthor(),
                inquiry.getCreatedDate().format(INQUIRY_DATE_FORMAT),
                inquiry.getStatus(),
                inquiry.getStatusClass()
        );
    }

    private String nextApplicationId() {
        return "jc-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String nextInquiryId() {
        return "inq-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
