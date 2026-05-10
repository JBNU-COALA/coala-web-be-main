package com.example.coalawebbackend.domain.instance.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(
        name = "instance_applications",
        indexes = {
                @Index(name = "idx_instance_applications_status", columnList = "status"),
                @Index(name = "idx_instance_applications_requested_at", columnList = "requested_at")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceApplication extends BaseEntity {

    @Id
    @Column(name = "application_id", length = 30)
    private String id;

    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "key_email", nullable = false, length = 120)
    private String keyEmail;

    @Column(name = "instance_type", nullable = false, length = 20)
    private String instanceType;

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "duration", nullable = false, length = 20)
    private String duration;

    @Column(name = "requested_at", nullable = false)
    private LocalDate requestedAt;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @ElementCollection
    @CollectionTable(name = "instance_application_files", joinColumns = @JoinColumn(name = "application_id"))
    @Builder.Default
    private List<InstanceAttachedFile> attachedFiles = new ArrayList<>();

    @Embedded
    private InstanceSpec specs;

    public void update(String instanceType, String duration, String purpose, String status, String adminNote) {
        if (StringUtils.hasText(instanceType)) {
            this.instanceType = instanceType;
            this.specs = InstanceSpec.forType(instanceType);
        }
        if (StringUtils.hasText(duration)) {
            this.duration = duration;
        }
        if (StringUtils.hasText(purpose)) {
            this.purpose = purpose;
        }
        if (StringUtils.hasText(status)) {
            this.status = status;
            if (("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) && approvedAt == null) {
                this.approvedAt = LocalDate.now();
            }
        }
        if (adminNote != null) {
            this.adminNote = adminNote;
        }
    }
}
