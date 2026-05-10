package com.example.coalawebbackend.domain.attachment.repository;

import com.example.coalawebbackend.domain.attachment.entity.Attachment;
import com.example.coalawebbackend.domain.attachment.entity.AttachmentStatus;
import com.example.coalawebbackend.domain.attachment.entity.AttachmentTargetType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByIdIn(Collection<Long> ids);

    List<Attachment> findByTargetTypeAndTargetIdAndStatus(
            AttachmentTargetType targetType,
            Long targetId,
            AttachmentStatus status
    );

    List<Attachment> findByStatusAndCreatedAtBefore(AttachmentStatus status, LocalDateTime createdAt);
}
