package com.example.coalawebbackend.domain.instance.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "service_inquiries",
        indexes = @Index(name = "idx_service_inquiries_status", columnList = "status")
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceInquiry extends BaseEntity {

    @Id
    @Column(name = "inquiry_id", length = 30)
    private String id;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_name", nullable = false, length = 50)
    private String author;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "status_class", nullable = false, length = 40)
    private String statusClass;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;
}
