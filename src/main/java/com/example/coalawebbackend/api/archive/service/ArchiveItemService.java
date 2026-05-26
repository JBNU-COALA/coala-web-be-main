package com.example.coalawebbackend.api.archive.service;

import com.example.coalawebbackend.api.archive.dto.ArchiveItemRequest;
import com.example.coalawebbackend.api.archive.dto.ArchiveItemResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.archive.entity.ArchiveCategory;
import com.example.coalawebbackend.domain.archive.entity.ArchiveItem;
import com.example.coalawebbackend.domain.archive.repository.ArchiveItemRepository;
import com.example.coalawebbackend.domain.attachment.service.AttachmentService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveItemService {

    private static final Pattern ATTACHMENT_DOWNLOAD_PATH = Pattern.compile(
            "^/(?:api|media)/attachments/(\\d+)/download(?:[?#].*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private final ArchiveItemRepository archiveItemRepository;
    private final PermissionService permissionService;
    private final AttachmentService attachmentService;

    public List<ArchiveItemResponse> getItems(String category, String query) {
        boolean hasCategory = StringUtils.hasText(category) && !"all".equalsIgnoreCase(category.trim());
        List<ArchiveItem> items = hasCategory
                ? archiveItemRepository.findByCategoryOrderByCreatedAtDescIdDesc(ArchiveCategory.from(category))
                : archiveItemRepository.findAllByOrderByCreatedAtDescIdDesc();
        String normalizedQuery = normalizeQuery(query);

        return items.stream()
                .filter(item -> normalizedQuery.isBlank() || matches(item, normalizedQuery))
                .map(ArchiveItemResponse::from)
                .toList();
    }

    public ArchiveItemResponse getItem(Long itemId) {
        return ArchiveItemResponse.from(getItemEntity(itemId));
    }

    @Transactional
    public ArchiveItemResponse createItem(User actor, ArchiveItemRequest request) {
        ArchiveItem item = ArchiveItem.create(
                ArchiveCategory.from(request.category()),
                actor,
                displayName(actor),
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                normalizeOptionalUrl(request.sourceUrl(), true),
                normalizeOptionalUrl(request.repositoryUrl(), false),
                normalizeTags(request.tags())
        );
        ArchiveItem saved = archiveItemRepository.save(item);
        attachmentService.syncArchiveAttachment(actor, saved.getId(), extractAttachmentId(saved.getSourceUrl()));
        return ArchiveItemResponse.from(saved);
    }

    @Transactional
    public ArchiveItemResponse updateItem(User actor, Long itemId, ArchiveItemRequest request) {
        ArchiveItem item = getItemEntity(itemId);
        assertCanManage(actor, item);
        item.update(
                ArchiveCategory.from(request.category()),
                request.title().trim(),
                request.summary().trim(),
                request.content().trim(),
                normalizeOptionalUrl(request.sourceUrl(), true),
                normalizeOptionalUrl(request.repositoryUrl(), false),
                normalizeTags(request.tags())
        );
        attachmentService.syncArchiveAttachment(actor, item.getId(), extractAttachmentId(item.getSourceUrl()));
        return ArchiveItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(User actor, Long itemId) {
        ArchiveItem item = getItemEntity(itemId);
        assertCanManage(actor, item);
        attachmentService.markArchiveAttachmentsDeleted(item.getId(), actor);
        archiveItemRepository.delete(item);
    }

    private ArchiveItem getItemEntity(Long itemId) {
        return archiveItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void assertCanManage(User actor, ArchiveItem item) {
        boolean isOwner = actor != null
                && item.getOwner() != null
                && actor.getId().equals(item.getOwner().getId());
        if (!isOwner && !permissionService.canModerate(actor)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean matches(ArchiveItem item, String query) {
        String searchable = String.join(" ",
                item.getTitle(),
                item.getSummary(),
                item.getContent(),
                item.getOwnerName(),
                item.getTags());
        return searchable.toLowerCase().contains(query);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase();
    }

    private String normalizeOptionalUrl(String url, boolean allowAttachmentPath) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        if (allowAttachmentPath && ATTACHMENT_DOWNLOAD_PATH.matcher(trimmed).matches()) {
            return trimmed;
        }
        try {
            URI parsed = URI.create(trimmed);
            String scheme = parsed.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Invalid URL scheme");
            }
            if (!StringUtils.hasText(parsed.getHost())) {
                throw new IllegalArgumentException("Invalid URL host");
            }
            return trimmed;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
    }

    private Long extractAttachmentId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Matcher relativeMatcher = ATTACHMENT_DOWNLOAD_PATH.matcher(value.trim());
        if (relativeMatcher.matches()) {
            return Long.parseLong(relativeMatcher.group(1));
        }
        try {
            URI parsed = URI.create(value.trim());
            Matcher absoluteMatcher = ATTACHMENT_DOWNLOAD_PATH.matcher(parsed.getPath());
            return absoluteMatcher.matches() ? Long.parseLong(absoluteMatcher.group(1)) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalizeTags(List<String> tags) {
        if (tags == null) {
            return "";
        }
        return tags.stream()
                .map(tag -> tag == null ? "" : tag.trim())
                .filter(tag -> !tag.isBlank())
                .distinct()
                .limit(8)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String displayName(User user) {
        if (user == null) {
            return "코알라";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        return user.getName();
    }
}
