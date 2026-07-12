package com.example.coalawebbackend.domain.anonymous.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 익명 게시판(BoardType.ANONYMOUS) 내에서 사용자별로 유지되는 익명 표시 프로필.
 * 하나의 (board, user) 조합당 하나의 표시명을 가지며, 해당 사용자 본인만 수정할 수 있다.
 * 실제 작성자(User)는 Post/Comment 엔티티에 그대로 저장되어 신고/제재 등 운영 기능에는 영향을 주지 않는다.
 */
@Entity
@Table(
        name = "anonymous_profiles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_anonymous_profiles_board_user",
                columnNames = {"board_id", "user_id"}
        )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnonymousProfile extends BaseEntity {

    public static final String DEFAULT_DISPLAY_NAME = "익명";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anonymous_profile_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    public static AnonymousProfile createDefault(Board board, User user) {
        return AnonymousProfile.builder()
                .board(board)
                .user(user)
                .displayName(DEFAULT_DISPLAY_NAME)
                .build();
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
