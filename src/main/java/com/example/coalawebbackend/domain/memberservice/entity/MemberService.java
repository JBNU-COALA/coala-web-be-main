package com.example.coalawebbackend.domain.memberservice.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "member_services",
        indexes = {
                @Index(name = "idx_member_services_category", columnList = "category"),
                @Index(name = "idx_member_services_status", columnList = "status")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberService extends BaseEntity {

    @Id
    @Column(name = "service_id", length = 80)
    private String id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "owner_name", nullable = false, length = 50)
    private String owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "service_url", nullable = false, length = 500)
    private String url;

    @Column(name = "github_url", nullable = false, length = 500)
    private String githubUrl;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "member_service_additional_images", joinColumns = @JoinColumn(name = "service_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "image_url", nullable = false, length = 500)
    @Builder.Default
    private List<String> additionalImageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "member_service_tags", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "audience", nullable = false, length = 100)
    private String audience;

    @Column(name = "visibility", nullable = false, length = 30)
    private String visibility;

    @Column(name = "period", nullable = false, length = 100)
    private String period;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "member_service_features", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "feature", nullable = false, length = 255)
    @Builder.Default
    private List<String> features = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "member_service_stack", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "stack_name", nullable = false, length = 80)
    @Builder.Default
    private List<String> stack = new ArrayList<>();

    public void updateCatalog(String title, String category, String owner, String summary, String url,
                              String githubUrl, String imageUrl, List<String> additionalImageUrls,
                              List<String> tags, String status) {
        this.title = title;
        this.category = category;
        this.owner = owner;
        this.summary = summary;
        this.url = url;
        this.githubUrl = githubUrl;
        this.imageUrl = imageUrl;
        this.additionalImageUrls = new ArrayList<>(additionalImageUrls == null ? List.of() : additionalImageUrls);
        this.tags = new ArrayList<>(tags == null ? List.of() : tags);
        this.status = status;
        this.period = status;
        if ("운영중지".equals(status)) {
            this.visibility = "Private";
            this.period = "운영 중지";
        } else if ("운영완료".equals(status) || "운영종료".equals(status)) {
            this.visibility = "Public";
            this.period = "운영 완료";
        } else {
            this.visibility = "Public";
            this.period = "운영 중";
        }
    }

    public void retire() {
        this.status = "운영중지";
        this.visibility = "Private";
        this.period = "운영 중지";
    }
}
