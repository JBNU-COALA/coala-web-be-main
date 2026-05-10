package com.example.coalawebbackend.domain.profile.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "public_user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_public_user_profiles_github", columnNames = "github_handle"),
        indexes = {
                @Index(name = "idx_public_user_profiles_grade", columnList = "grade_label"),
                @Index(name = "idx_public_user_profiles_lab", columnList = "lab")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicUserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "initials", nullable = false, length = 10)
    private String initials;

    @Column(name = "tone", nullable = false, length = 20)
    private String tone;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "grade_label", nullable = false, length = 20)
    private String grade;

    @Column(name = "lab", nullable = false, length = 100)
    private String lab;

    @Column(name = "github_handle", nullable = false, length = 39)
    private String githubHandle;

    @Column(name = "github_url", nullable = false, length = 255)
    private String githubUrl;

    @Column(name = "focus", nullable = false, length = 255)
    private String focus;

    @Column(name = "recent_commit", nullable = false, length = 50)
    private String recentCommit;

    @ElementCollection
    @CollectionTable(
            name = "public_user_profile_repositories",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "repository_name", nullable = false, length = 100)
    @Builder.Default
    private List<String> sharedRepos = new ArrayList<>();

    @Column(name = "solved_handle", nullable = false, length = 50)
    private String solvedHandle;

    @Column(name = "solved_tier", nullable = false, length = 20)
    private String solvedTier;

    @Column(name = "solved_count", nullable = false)
    private Integer solvedCount;

    @Column(name = "github_commits", nullable = false)
    private Integer githubCommits;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "is_me", nullable = false)
    @Builder.Default
    private boolean isMe = false;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<PublicUserActivityLog> logs = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<PublicUserAward> awards = new ArrayList<>();

    public void addLog(PublicUserActivityLog log) {
        logs.add(log);
        log.attachTo(this);
    }

    public void addAward(PublicUserAward award) {
        awards.add(award);
        award.attachTo(this);
    }
}
