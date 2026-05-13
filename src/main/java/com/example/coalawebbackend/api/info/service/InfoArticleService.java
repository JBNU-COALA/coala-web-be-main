package com.example.coalawebbackend.api.info.service;

import com.example.coalawebbackend.api.info.dto.InfoArticleRequest;
import com.example.coalawebbackend.api.info.dto.InfoArticleResponse;
import com.example.coalawebbackend.api.notification.service.NotificationService;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoCategory;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfoArticleService {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final InfoArticleRepository infoArticleRepository;
    private final PermissionService permissionService;
    private final NotificationService notificationService;

    public List<InfoArticleResponse> getArticles(String filter, String query) {
        InfoCategory category = InfoCategory.from(filter);
        List<InfoArticle> articles = StringUtils.hasText(filter) && !"all".equalsIgnoreCase(filter)
                ? infoArticleRepository.findByCategoryOrderBySourceDateDescIdDesc(category)
                : infoArticleRepository.findAllByOrderBySourceDateDescIdDesc();

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        return articles.stream()
                .filter(article -> normalizedQuery.isBlank() || matches(article, normalizedQuery))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InfoArticleResponse getArticle(Long articleId) {
        InfoArticle article = getArticleEntity(articleId);
        article.increaseViewCount();
        return toResponse(article);
    }

    @Transactional
    public InfoArticleResponse createArticle(User actor, InfoArticleRequest request) {
        permissionService.assertModerator(actor);
        InfoArticle article = InfoArticle.builder()
                .category(InfoCategory.from(request.filter()))
                .tag(request.tag())
                .title(request.title())
                .meta(request.meta())
                .sourceName(request.sourceName())
                .sourceDate(LocalDate.parse(request.sourceDate()))
                .content(request.content())
                .imageUrl(blankToEmpty(request.imageUrl()))
                .build();
        InfoArticle savedArticle = infoArticleRepository.save(article);
        notificationService.notifyInterestedInfo(actor, savedArticle);
        return toResponse(savedArticle);
    }

    @Transactional
    public InfoArticleResponse updateArticle(User actor, Long articleId, InfoArticleRequest request) {
        permissionService.assertModerator(actor);
        InfoArticle article = getArticleEntity(articleId);
        article.update(
                InfoCategory.from(request.filter()),
                request.tag(),
                request.title(),
                request.meta(),
                request.sourceName(),
                LocalDate.parse(request.sourceDate()),
                request.content(),
                blankToEmpty(request.imageUrl()));
        return toResponse(article);
    }

    @Transactional
    public void deleteArticle(User actor, Long articleId) {
        permissionService.assertModerator(actor);
        infoArticleRepository.delete(getArticleEntity(articleId));
    }

    @Transactional
    public InfoArticleResponse bookmarkArticle(Long articleId) {
        InfoArticle article = getArticleEntity(articleId);
        article.increaseBookmarkCount();
        return toResponse(article);
    }

    private InfoArticle getArticleEntity(Long articleId) {
        return infoArticleRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private boolean matches(InfoArticle article, String query) {
        return (article.getTitle() + " " + article.getMeta() + " " + article.getSourceName() + " " + article.getTag())
                .toLowerCase()
                .contains(query);
    }

    private String blankToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private InfoArticleResponse toResponse(InfoArticle article) {
        return new InfoArticleResponse(
                article.getId(),
                article.getCategory().getApiValue(),
                article.getTag(),
                article.getTitle(),
                article.getMeta(),
                article.getSourceName() + " | " + article.getSourceDate().format(DISPLAY_DATE_FORMAT),
                article.getSourceName(),
                article.getSourceDate().toString(),
                article.getContent(),
                article.getImageUrl(),
                article.getViewCount(),
                article.getBookmarkCount(),
                article.getCreatedAt() == null ? null : article.getCreatedAt().toString(),
                article.getUpdatedAt() == null ? null : article.getUpdatedAt().toString()
        );
    }
}
