package com.example.coalawebbackend.domain.archive.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "archive_items",
        indexes = {
                @Index(name = "idx_archive_items_category", columnList = "category"),
                @Index(name = "idx_archive_items_owner", columnList = "owner_user_id"),
                @Index(name = "idx_archive_items_created", columnList = "created_at")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ArchiveItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_item_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ArchiveCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Column(name = "owner_name", nullable = false, length = 80)
    private String ownerName;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "summary", nullable = false, length = 500)
    private String summary;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url", nullable = false, length = 500)
    private String sourceUrl;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "tags", nullable = false, length = 500)
    private String tags;

    public static ArchiveItem create(
            ArchiveCategory category,
            User owner,
            String ownerName,
            String title,
            String summary,
            String content,
            String sourceUrl,
            String repositoryUrl,
            String tags
    ) {
        return ArchiveItem.builder()
                .category(category)
                .owner(owner)
                .ownerName(ownerName)
                .title(title)
                .summary(summary)
                .content(content)
                .sourceUrl(sourceUrl)
                .repositoryUrl(repositoryUrl)
                .tags(tags)
                .build();
    }

    public void update(
            ArchiveCategory category,
            String title,
            String summary,
            String content,
            String sourceUrl,
            String repositoryUrl,
            String tags
    ) {
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.repositoryUrl = repositoryUrl;
        this.tags = tags;
    }
}
