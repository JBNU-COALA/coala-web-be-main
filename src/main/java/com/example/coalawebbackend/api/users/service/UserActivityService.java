package com.example.coalawebbackend.api.users.service;

import com.example.coalawebbackend.api.users.dto.UserActivityItemResponse;
import com.example.coalawebbackend.api.users.dto.UserOverviewResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.BoardType;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.instance.entity.DomainApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import com.example.coalawebbackend.domain.instance.repository.DomainApplicationRepository;
import com.example.coalawebbackend.domain.instance.repository.InstanceApplicationRepository;
import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import com.example.coalawebbackend.domain.memberservice.repository.MemberServiceRepository;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.recruit.entity.RecruitApplication;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.repository.RecruitApplicationRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitBookmarkRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitPostRepository;
import com.example.coalawebbackend.domain.study.StudyGroupRepository;
import com.example.coalawebbackend.domain.study.StudyRecord;
import com.example.coalawebbackend.domain.study.StudyRecordRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    private final RecruitApplicationRepository recruitApplicationRepository;
    private final RecruitBookmarkRepository recruitBookmarkRepository;
    private final MemberServiceRepository memberServiceRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudyRecordRepository studyRecordRepository;

    public List<UserActivityItemResponse> getMyActivities(Long currentUserId) {
        return getOverview(currentUserId, currentUserId).items();
    }

    public UserOverviewResponse getOverview(Long userId, Long currentUserId) {
        if (currentUserId == null) throw new CustomException(ErrorCode.ACCESS_DENIED);
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!actor.isVerified()) throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        if (userId == null || (!currentUserId.equals(userId) && !userRepository.existsById(userId))) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        boolean self = currentUserId.equals(userId);
        List<ActivityItem> items = new ArrayList<>();
        postRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .filter(post -> self || (post.isVisible() && Boolean.TRUE.equals(post.getBoard().getIsActive())
                        && post.getBoard().getType() != BoardType.ANONYMOUS
                        && (post.getBoard().getType() != BoardType.NORMAL || post.getBoard().getCategoryKey() != null)))
                .forEach(post -> items.add(boardPost(post)));
        infoArticleRepository.findByAuthor_IdOrderBySourceDateDescIdDesc(userId)
                .forEach(article -> items.add(infoArticle(article)));
        recruitPostRepository.findByAuthor_IdOrderByCreatedAtDesc(userId)
                .forEach(recruit -> items.add(recruitPost(recruit)));
        memberServiceRepository.findByOwnerUser_IdOrderByCreatedAtDesc(userId).stream()
                .filter(service -> self || "Public".equalsIgnoreCase(service.getVisibility()))
                .forEach(service -> items.add(memberService(service)));
        studyRecordRepository.findForMember(userId).forEach(record -> items.add(studyRecord(record)));
        long pending = 0;
        if (self) {
            List<RecruitApplication> applications = recruitApplicationRepository.findByUser_IdOrderBySubmittedAtDesc(userId);
            applications.forEach(application -> items.add(recruitApplication(application)));
            pending = applications.stream().filter(application -> "submitted".equals(application.getStatus())).count();
            instanceApplicationRepository.findByUser_IdOrderByRequestedAtDesc(userId)
                    .forEach(application -> items.add(instanceApplication(application)));
            domainApplicationRepository.findByUser_IdOrderByRequestedAtDesc(userId)
                    .forEach(application -> items.add(domainApplication(application)));
        }

        List<UserActivityItemResponse> responses = items.stream()
                .sorted(Comparator.comparing(ActivityItem::sortAt).reversed()
                        .thenComparing(item -> item.response().id()))
                .map(ActivityItem::response)
                .toList();
        return new UserOverviewResponse(self, responses, new UserOverviewResponse.Counts(
                studyGroupRepository.countForMember(userId), count(responses, "study"), count(responses, "board"),
                count(responses, "info"), count(responses, "recruit"), count(responses, "service"),
                self ? count(responses, "recruit-application") : null, self ? pending : null,
                self ? recruitBookmarkRepository.countByUser_Id(userId) : null,
                self ? count(responses, "instance") : null, self ? count(responses, "domain") : null));
    }

    private long count(List<UserActivityItemResponse> items, String kind) {
        return items.stream().filter(item -> kind.equals(item.kind())).count();
    }

    private ActivityItem studyRecord(StudyRecord record) {
        return new ActivityItem(new UserActivityItemResponse(
                "study-" + record.getId(), "study", "활동", record.getTitle(), preview(record.getContent()),
                "ACTIVE", record.getGroup() == null ? "" : record.getGroup().getName(), null, null,
                record.getId(), null, record.getDate().toString()),
                LocalDateTime.ofInstant(record.getUpdatedAt(), ZoneId.of("Asia/Seoul")));
    }

    private ActivityItem memberService(MemberService service) {
        return new ActivityItem(new UserActivityItemResponse(
                "service-" + service.getId(), "service", "서비스", service.getTitle(), service.getSummary(),
                service.getStatus(), service.getCategory(), null, null, service.getId(), null,
                toCreatedAt(service.getCreatedAt())), fallbackNow(service.getCreatedAt()));
    }

    private ActivityItem recruitApplication(RecruitApplication application) {
        RecruitPost recruit = application.getRecruitPost();
        return new ActivityItem(new UserActivityItemResponse(
                "recruit-application-" + application.getId(), "recruit-application", "모집 지원", recruit.getTitle(),
                preview(application.getBody()), application.getStatus(), application.getRole(), null, null,
                recruit.getId(), null, toCreatedAt(application.getSubmittedAt())), fallbackNow(application.getSubmittedAt()));
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
