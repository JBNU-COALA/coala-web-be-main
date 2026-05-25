package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.users.dto.UserActivityItemResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.instance.entity.DomainApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import com.example.coalawebbackend.domain.instance.repository.DomainApplicationRepository;
import com.example.coalawebbackend.domain.instance.repository.InstanceApplicationRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.repository.RecruitPostRepository;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final InfoArticleRepository infoArticleRepository;
    private final RecruitPostRepository recruitPostRepository;
    private final InstanceApplicationRepository instanceApplicationRepository;
    private final DomainApplicationRepository domainApplicationRepository;

    public List<UserActivityItemResponse> getMyActivities(Long currentUserId) {
        if (currentUserId == null || !userRepository.existsById(currentUserId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<ActivityItem> items = new ArrayList<>();
        postRepository.findByUser_IdOrderByCreatedAtDesc(currentUserId)
                .forEach(post -> items.add(boardPost(post)));
        infoArticleRepository.findByAuthor_IdOrderBySourceDateDescIdDesc(currentUserId)
                .forEach(article -> items.add(infoArticle(article)));
        recruitPostRepository.findByAuthor_IdOrderByCreatedAtDesc(currentUserId)
                .forEach(recruit -> items.add(recruitPost(recruit)));
        instanceApplicationRepository.findByUser_IdOrderByRequestedAtDesc(currentUserId)
                .forEach(application -> items.add(instanceApplication(application)));
        domainApplicationRepository.findByUser_IdOrderByRequestedAtDesc(currentUserId)
                .forEach(application -> items.add(domainApplication(application)));

        return items.stream()
                .sorted(Comparator.comparing(ActivityItem::sortAt).reversed())
                .map(ActivityItem::response)
                .toList();
    }

    private ActivityItem boardPost(Post post) {
        UserActivityItemResponse response = new UserActivityItemResponse(
                "post-" + post.getPostId(),
                "board",
                "게시판",
                post.getTitle(),
                preview(post.getContent()),
                post.getStatus().name(),
                post.getBoard().getName(),
                post.getBoard().getBoardId(),
                post.getPostId(),
                null,
                (long) post.getViewCount(),
                toCreatedAt(post.getCreatedAt())
        );
        return new ActivityItem(response, fallbackNow(post.getCreatedAt()));
    }

    private ActivityItem infoArticle(InfoArticle article) {
        UserActivityItemResponse response = new UserActivityItemResponse(
                "info-" + article.getId(),
                "info",
                "정보공유",
                article.getTitle(),
                preview(article.getContent()),
                "ACTIVE",
                article.getCategory().getApiValue(),
                null,
                article.getId(),
                null,
                article.getViewCount(),
                toCreatedAt(article.getCreatedAt())
        );
        return new ActivityItem(response, fallbackNow(article.getCreatedAt()));
    }

    private ActivityItem recruitPost(RecruitPost recruit) {
        UserActivityItemResponse response = new UserActivityItemResponse(
                "recruit-" + recruit.getId(),
                "recruit",
                "모집",
                recruit.getTitle(),
                recruit.getShortDesc(),
                recruit.getStatus(),
                recruit.getCategory(),
                null,
                null,
                recruit.getId(),
                recruit.getViews(),
                toCreatedAt(recruit.getCreatedAt())
        );
        return new ActivityItem(response, fallbackNow(recruit.getCreatedAt()));
    }

    private ActivityItem instanceApplication(InstanceApplication application) {
        UserActivityItemResponse response = new UserActivityItemResponse(
                "instance-" + application.getId(),
                "instance",
                "인스턴스 신청",
                application.getInstanceType() + " 인스턴스",
                application.getPurpose(),
                application.getStatus(),
                application.getDuration(),
                null,
                null,
                application.getId(),
                null,
                application.getRequestedAt().toString()
        );
        return new ActivityItem(response, application.getRequestedAt().atStartOfDay());
    }

    private ActivityItem domainApplication(DomainApplication application) {
        UserActivityItemResponse response = new UserActivityItemResponse(
                "domain-" + application.getId(),
                "domain",
                "도메인 신청",
                application.getServiceName(),
                application.getRequestedDomain() + " · " + application.getPurpose(),
                application.getStatus(),
                application.getDesiredAddress(),
                null,
                null,
                application.getId(),
                null,
                application.getRequestedAt().toString()
        );
        return new ActivityItem(response, application.getRequestedAt().atTime(LocalTime.NOON));
    }

    private String preview(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content
                .replaceAll("!\\[[^]]*]\\([^)]*\\)", " ")
                .replaceAll("\\[[^]]+]\\([^)]*\\)", " ")
                .replaceAll("<[^>]+>", " ")
                .replaceAll("[#>*_`~-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.length() > 140 ? normalized.substring(0, 140) : normalized;
    }

    private String toCreatedAt(LocalDateTime createdAt) {
        return createdAt == null ? "" : createdAt.toString();
    }

    private LocalDateTime fallbackNow(LocalDateTime createdAt) {
        return createdAt == null ? LocalDateTime.MIN : createdAt;
    }

    private record ActivityItem(UserActivityItemResponse response, LocalDateTime sortAt) {
    }
}
