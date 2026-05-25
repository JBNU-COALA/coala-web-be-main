package com.example.coalawebbackend.domain.instance.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(
        name = "domain_applications",
        indexes = {
                @Index(name = "idx_domain_applications_status", columnList = "status"),
                @Index(name = "idx_domain_applications_requested_at", columnList = "requested_at"),
                @Index(name = "idx_domain_applications_user", columnList = "user_id")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainApplication extends BaseEntity {

    @Id
    @Column(name = "application_id", length = 30)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "contact_email", nullable = false, length = 120)
    private String contactEmail;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "desired_address", nullable = false, length = 60)
    private String desiredAddress;

    @Column(name = "requested_domain", nullable = false, length = 160)
    private String requestedDomain;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "requested_at", nullable = false)
    private LocalDate requestedAt;

    @Column(name = "processed_at")
    private LocalDate processedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    public void update(String status, String adminNote) {
        if (StringUtils.hasText(status)) {
            this.status = status.trim();
            if (("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) && processedAt == null) {
                this.processedAt = LocalDate.now();
            }
        }
        if (adminNote != null) {
            this.adminNote = adminNote;
        }
    }
}
