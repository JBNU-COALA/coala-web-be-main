package com.example.coalawebbackend.domain.recruit.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
        name = "recruit_posts",
        indexes = {
                @Index(name = "idx_recruit_posts_category", columnList = "category"),
                @Index(name = "idx_recruit_posts_status", columnList = "status")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecruitPost extends BaseEntity {

    @Id
    @Column(name = "recruit_id", length = 80)
    private String id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "short_desc", nullable = false, length = 300)
    private String shortDesc;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "current_members", nullable = false)
    private int currentMembers;

    @Column(name = "max_members", nullable = false)
    private int maxMembers;

    @Column(name = "host", nullable = false, length = 50)
    private String host;

    @Column(name = "host_initials", nullable = false, length = 10)
    private String hostInitials;

    @Column(name = "host_tone", nullable = false, length = 20)
    private String hostTone;

    @Column(name = "host_role", nullable = false, length = 100)
    private String hostRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "trust_score", nullable = false)
    private double trustScore;

    @ElementCollection
    @CollectionTable(name = "recruit_tags", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "recruit_tech_stack", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "tech_name", nullable = false, length = 80)
    @Builder.Default
    private List<String> techStack = new ArrayList<>();

    @OneToMany(mappedBy = "recruitPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<RecruitRole> roles = new ArrayList<>();

    @Column(name = "meeting_type", nullable = false, length = 150)
    private String meetingType;

    @Column(name = "expected_duration", nullable = false, length = 80)
    private String expectedDuration;

    @ElementCollection
    @CollectionTable(name = "recruit_detail_paragraphs", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "paragraph", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private List<String> detailContent = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "recruit_process_items", joinColumns = @JoinColumn(name = "recruit_id"))
    @Column(name = "process_item", nullable = false, length = 255)
    @Builder.Default
    private List<String> processList = new ArrayList<>();

    @Column(name = "views", nullable = false)
    private long views;

    @Column(name = "bookmarks", nullable = false)
    private long bookmarks;

    public void addRole(RecruitRole role) {
        roles.add(role);
        role.attachTo(this);
    }

    public void clearRoles() {
        roles.clear();
    }

    public void update(
            String title,
            String shortDesc,
            String category,
            String status,
            int maxMembers,
            List<String> tags,
            List<String> techStack,
            String meetingType,
            String expectedDuration,
            List<String> detailContent,
            List<String> processList) {
        this.title = title;
        this.shortDesc = shortDesc;
        this.category = category;
        this.status = status;
        this.maxMembers = Math.max(maxMembers, 1);
        this.currentMembers = Math.min(this.currentMembers, this.maxMembers);
        this.tags.clear();
        this.tags.addAll(tags);
        this.techStack.clear();
        this.techStack.addAll(techStack);
        this.meetingType = meetingType;
        this.expectedDuration = expectedDuration;
        this.detailContent.clear();
        this.detailContent.addAll(detailContent);
        this.processList.clear();
        this.processList.addAll(processList);
    }

    public void increaseViews() {
        this.views++;
    }

    public void increaseBookmarks() {
        this.bookmarks++;
    }
}
