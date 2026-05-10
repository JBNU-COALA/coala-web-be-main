package com.example.coalawebbackend.infra.scheduler;

import com.example.coalawebbackend.domain.attachment.entity.Attachment;
import com.example.coalawebbackend.domain.attachment.entity.AttachmentStatus;
import com.example.coalawebbackend.domain.attachment.service.AttachmentService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AttachmentCleanupScheduler {

    private final AttachmentService attachmentService;

    @Scheduled(cron = "0 20 3 * * *")
    @Transactional
    public void cleanupTempAttachments() {
        cleanup(AttachmentStatus.TEMP, LocalDateTime.now().minusHours(24), true);
    }

    @Scheduled(cron = "0 40 3 * * *")
    @Transactional
    public void cleanupDeletedAttachments() {
        cleanup(AttachmentStatus.DELETED, LocalDateTime.now().minusDays(30), false);
    }

    private void cleanup(AttachmentStatus status, LocalDateTime before, boolean markOrphaned) {
        for (Attachment attachment : attachmentService.findCleanupTargets(status, before)) {
            attachmentService.deletePhysicalFile(attachment);
            if (markOrphaned) {
                attachment.markOrphaned();
            }
        }
    }
}
