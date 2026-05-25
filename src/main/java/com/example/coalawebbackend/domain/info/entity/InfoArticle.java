package com.example.coalawebbackend.domain.info.entity;

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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "info_articles",
        indexes = {
                @Index(name = "idx_info_articles_category", columnList = "category"),
                @Index(name = "idx_info_articles_source_date", columnList = "source_date")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InfoArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_article_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private InfoCategory category;

    @Column(name = "tag", nullable = false, length = 30)
    private String tag;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "meta", nullable = false, length = 100)
    private String meta;

    @Column(name = "source_name", nullable = false, length = 50)
    private String sourceName;

    @Column(name = "source_date", nullable = false)
    private LocalDate sourceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private long viewCount = 0;

    @Column(name = "bookmark_count", nullable = false)
    @Builder.Default
    private long bookmarkCount = 0;

    public void update(InfoCategory category, String tag, String title, String meta,
                       String sourceName, LocalDate sourceDate, String content, String imageUrl) {
        this.category = category;
        this.tag = tag;
        this.title = title;
        this.meta = meta;
        this.sourceName = sourceName;
        this.sourceDate = sourceDate;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }
}
