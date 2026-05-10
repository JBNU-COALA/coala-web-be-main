package com.example.coalawebbackend.domain.attachment.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "attachments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    private AttachmentTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", nullable = false, length = 30)
    private FileCategory fileCategory;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "extension", length = 20)
    private String extension;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Builder.Default
    @Column(name = "is_representative", nullable = false)
    private boolean representative = false;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AttachmentStatus status = AttachmentStatus.TEMP;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    public void activate(AttachmentTargetType targetType, Long targetId, int displayOrder, boolean representative) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.displayOrder = displayOrder;
        this.representative = representative;
        this.status = AttachmentStatus.ACTIVE;
    }

    public void markDeleted(User actor) {
        this.status = AttachmentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = actor;
    }

    public void markOrphaned() {
        this.status = AttachmentStatus.ORPHANED;
    }

    public boolean isUploadedBy(User user) {
        return user != null && uploader != null && uploader.getId().equals(user.getId());
    }

    public boolean isActive() {
        return getStatus() == AttachmentStatus.ACTIVE;
    }

    public boolean isTemp() {
        return getStatus() == AttachmentStatus.TEMP;
    }

    public AttachmentStatus getStatus() {
        return status == null ? AttachmentStatus.TEMP : status;
    }
}
