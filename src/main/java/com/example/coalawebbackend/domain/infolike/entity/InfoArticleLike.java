package com.example.coalawebbackend.domain.infolike.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "info_article_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_info_article_like_user_article",
                columnNames = {"user_id", "info_article_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class InfoArticleLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_article_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_article_id", nullable = false)
    private InfoArticle article;

    public static InfoArticleLike create(User user, InfoArticle article) {
        return InfoArticleLike.builder()
                .user(user)
                .article(article)
                .build();
    }
}
