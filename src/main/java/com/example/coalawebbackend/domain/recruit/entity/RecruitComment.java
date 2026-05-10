package com.example.coalawebbackend.domain.recruit.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "recruit_comments",
        indexes = @Index(name = "idx_recruit_comments_recruit", columnList = "recruit_id")
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecruitComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruit_id", nullable = false)
    private RecruitPost recruitPost;

    @Column(name = "author", nullable = false, length = 50)
    private String author;

    @Column(name = "author_initials", nullable = false, length = 10)
    private String authorInitials;

    @Column(name = "author_tone", nullable = false, length = 20)
    private String authorTone;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
