package com.example.coalawebbackend.domain.post.entity;

import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.common.entity.BaseEntity;
import com.example.coalawebbackend.domain.board.entity.Board;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    public static Post create(PostRequest dto,
                                      Board board,
                                      User user) {
        return Post.builder()
                .board(board)
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
    }

    public void update(PostRequest dto) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
    }
}
