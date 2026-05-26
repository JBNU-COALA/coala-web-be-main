package com.example.coalawebbackend.domain.attachment.service;

import com.example.coalawebbackend.api.attachment.dto.AttachmentDownloadResponse;
import com.example.coalawebbackend.api.attachment.dto.AttachmentUploadResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.attachment.entity.Attachment;
import com.example.coalawebbackend.domain.attachment.entity.AttachmentStatus;
import com.example.coalawebbackend.domain.attachment.entity.AttachmentTargetType;
import com.example.coalawebbackend.domain.attachment.entity.FileCategory;
import com.example.coalawebbackend.domain.attachment.repository.AttachmentRepository;
import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.memberservice.repository.MemberServiceRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.example.coalawebbackend.infra.storage.FileStorage;
import com.example.coalawebbackend.infra.storage.StoredFile;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final InfoArticleRepository infoArticleRepository;
    private final MemberServiceRepository memberServiceRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

    @Transactional
    public AttachmentUploadResponse uploadImage(User uploader, MultipartFile file) {
        return upload(uploader, file, FileCategory.IMAGE);
    }

    @Transactional
    public AttachmentUploadResponse uploadFile(User uploader, MultipartFile file) {
        return upload(uploader, file, FileCategory.ATTACHMENT);
    }

    @Transactional(readOnly = true)
    public AttachmentDownloadResponse getDownload(Long attachmentId) {
        Attachment attachment = getAttachment(attachmentId);
        validateDownloadable(attachment);
        if (!fileStorage.exists(attachment.getStoragePath())) {
            throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        return new AttachmentDownloadResponse(
                fileStorage.load(attachment.getStoragePath()),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getFileSize()
        );
    }

    @Transactional
    public Long syncPostAttachments(User actor, Post post, List<Long> attachmentIds, Long thumbnailAttachmentId) {
        List<Long> normalizedIds = normalizeIds(attachmentIds);
        if (thumbnailAttachmentId != null && !normalizedIds.contains(thumbnailAttachmentId)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }

        List<Attachment> currentAttachments = attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                AttachmentTargetType.POST,
                post.getPostId(),
                AttachmentStatus.ACTIVE
        );
        Map<Long, Attachment> requestedAttachments = loadRequestedAttachments(normalizedIds);

        currentAttachments.stream()
                .filter(attachment -> !normalizedIds.contains(attachment.getId()))
                .forEach(attachment -> attachment.markDeleted(actor));

        for (int i = 0; i < normalizedIds.size(); i++) {
            Long attachmentId = normalizedIds.get(i);
            Attachment attachment = requestedAttachments.get(attachmentId);
            validateAttachable(actor, post, attachment);
            attachment.activate(
                    AttachmentTargetType.POST,
                    post.getPostId(),
                    i,
                    thumbnailAttachmentId != null && thumbnailAttachmentId.equals(attachmentId)
            );
        }
        return thumbnailAttachmentId;
    }

    @Transactional
    public Long syncInfoArticleAttachments(
            User actor,
            Long articleId,
            List<Long> attachmentIds,
            Long thumbnailAttachmentId
    ) {
        List<Long> normalizedIds = normalizeIds(attachmentIds);
        if (thumbnailAttachmentId != null && !normalizedIds.contains(thumbnailAttachmentId)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }

        List<Attachment> currentAttachments = attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                AttachmentTargetType.INFO_ARTICLE,
                articleId,
                AttachmentStatus.ACTIVE
        );
        Map<Long, Attachment> requestedAttachments = loadRequestedAttachments(normalizedIds);

        currentAttachments.stream()
                .filter(attachment -> !normalizedIds.contains(attachment.getId()))
                .forEach(attachment -> attachment.markDeleted(actor));

        for (int i = 0; i < normalizedIds.size(); i++) {
            Long attachmentId = normalizedIds.get(i);
            Attachment attachment = requestedAttachments.get(attachmentId);
            validateInfoArticleAttachable(actor, articleId, attachment);
            attachment.activate(
                    AttachmentTargetType.INFO_ARTICLE,
                    articleId,
                    i,
                    thumbnailAttachmentId != null && thumbnailAttachmentId.equals(attachmentId)
            );
        }
        return thumbnailAttachmentId;
    }

    @Transactional(readOnly = true)
    public List<Attachment> findActiveInfoArticleAttachments(Long articleId) {
        return attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                AttachmentTargetType.INFO_ARTICLE,
                articleId,
                AttachmentStatus.ACTIVE
        );
    }

    @Transactional
    public void markPostAttachmentsDeleted(Post post, User actor) {
        attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                        AttachmentTargetType.POST,
                        post.getPostId(),
                        AttachmentStatus.ACTIVE
                )
                .forEach(attachment -> attachment.markDeleted(actor));
    }

    @Transactional
    public void syncArchiveAttachment(User actor, Long archiveItemId, Long attachmentId) {
        List<Attachment> currentAttachments = attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                AttachmentTargetType.ARCHIVE,
                archiveItemId,
                AttachmentStatus.ACTIVE
        );

        if (attachmentId == null) {
            currentAttachments.forEach(attachment -> attachment.markDeleted(actor));
            return;
        }

        Attachment attachment = loadRequestedAttachments(List.of(attachmentId)).get(attachmentId);
        validateArchiveAttachable(actor, archiveItemId, attachment);

        currentAttachments.stream()
                .filter(current -> !attachmentId.equals(current.getId()))
                .forEach(current -> current.markDeleted(actor));

        attachment.activate(AttachmentTargetType.ARCHIVE, archiveItemId, 0, true);
    }

    @Transactional
    public void markArchiveAttachmentsDeleted(Long archiveItemId, User actor) {
        attachmentRepository.findByTargetTypeAndTargetIdAndStatus(
                        AttachmentTargetType.ARCHIVE,
                        archiveItemId,
                        AttachmentStatus.ACTIVE
                )
                .forEach(attachment -> attachment.markDeleted(actor));
    }

    @Transactional
    public void deletePhysicalFile(Attachment attachment) {
        fileStorage.delete(attachment.getStoragePath());
    }

    public List<Attachment> findCleanupTargets(AttachmentStatus status, java.time.LocalDateTime before) {
        return attachmentRepository.findByStatusAndCreatedAtBefore(status, before);
    }

    @Transactional(readOnly = true)
    public boolean isReferencedByPublishedContent(Attachment attachment) {
        if (attachment == null || attachment.getId() == null) {
            return false;
        }
        String legacyDownloadPath = "/api/attachments/" + attachment.getId() + "/download";
        String publicDownloadPath = "/media/attachments/" + attachment.getId() + "/download";
        return postRepository.existsByContentReference(legacyDownloadPath, PostStatus.ACTIVE)
                || postRepository.existsByContentReference(publicDownloadPath, PostStatus.ACTIVE)
                || infoArticleRepository.existsByContentOrImageReference(legacyDownloadPath)
                || infoArticleRepository.existsByContentOrImageReference(publicDownloadPath)
                || memberServiceRepository.existsByImageReference(legacyDownloadPath)
                || memberServiceRepository.existsByImageReference(publicDownloadPath)
                || userRepository.existsByProfileCustomizationContaining(legacyDownloadPath)
                || userRepository.existsByProfileCustomizationContaining(publicDownloadPath);
    }

    private AttachmentUploadResponse upload(User uploader, MultipartFile file, FileCategory category) {
        StoredFile storedFile = fileStorage.store(file, category);
        Attachment attachment = Attachment.builder()
                .uploader(uploader)
                .fileCategory(category)
                .originalName(storedFile.originalName())
                .storedName(storedFile.storedName())
                .storagePath(storedFile.storagePath())
                .contentType(storedFile.contentType())
                .fileSize(storedFile.fileSize())
                .extension(storedFile.extension())
                .checksum(storedFile.checksum())
                .status(AttachmentStatus.TEMP)
                .build();
        return AttachmentUploadResponse.from(attachmentRepository.save(attachment));
    }

    private Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND));
    }

    private void validateDownloadable(Attachment attachment) {
        if (attachment.isTemp() && attachment.getTargetType() == null && attachment.getTargetId() == null) {
            return;
        }
        if (!attachment.isActive()) {
            throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        if (attachment.getTargetType() == AttachmentTargetType.POST) {
            Post post = postRepository.findById(attachment.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND));
            if (post.getStatus() != PostStatus.ACTIVE) {
                throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            return;
        }
        if (attachment.getTargetType() == AttachmentTargetType.COMMENT) {
            Comment comment = commentRepository.findById(attachment.getTargetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND));
            if (!comment.isVisible() || !comment.getPost().isVisible()) {
                throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            return;
        }
        if (attachment.getTargetType() == AttachmentTargetType.USER) {
            return;
        }
        if (attachment.getTargetType() == AttachmentTargetType.INFO_ARTICLE) {
            if (!infoArticleRepository.existsById(attachment.getTargetId())) {
                throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            return;
        }
        if (attachment.getTargetType() == AttachmentTargetType.ARCHIVE) {
            return;
        }
        throw new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND);
    }

    private List<Long> normalizeIds(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(attachmentIds.stream()
                .filter(id -> id != null && id > 0)
                .toList()));
    }

    private Map<Long, Attachment> loadRequestedAttachments(List<Long> attachmentIds) {
        if (attachmentIds.isEmpty()) {
            return Map.of();
        }
        List<Attachment> attachments = attachmentRepository.findByIdIn(attachmentIds);
        if (attachments.size() != attachmentIds.size()) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }
        return attachments.stream().collect(Collectors.toMap(Attachment::getId, Function.identity()));
    }

    private void validateAttachable(User actor, Post post, Attachment attachment) {
        if (!attachment.isUploadedBy(actor)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (attachment.isTemp()) {
            return;
        }
        if (attachment.isActive()
                && attachment.getTargetType() == AttachmentTargetType.POST
                && post.getPostId().equals(attachment.getTargetId())) {
            return;
        }
        throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
    }

    private void validateInfoArticleAttachable(User actor, Long articleId, Attachment attachment) {
        if (attachment.isActive()
                && attachment.getTargetType() == AttachmentTargetType.INFO_ARTICLE
                && articleId.equals(attachment.getTargetId())) {
            return;
        }
        if (!attachment.isUploadedBy(actor)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (attachment.isTemp()) {
            return;
        }
        throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
    }

    private void validateArchiveAttachable(User actor, Long archiveItemId, Attachment attachment) {
        if (attachment.isActive()
                && attachment.getTargetType() == AttachmentTargetType.ARCHIVE
                && archiveItemId.equals(attachment.getTargetId())) {
            return;
        }
        if (!attachment.isUploadedBy(actor)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (attachment.isTemp()) {
            return;
        }
        throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
    }
}
