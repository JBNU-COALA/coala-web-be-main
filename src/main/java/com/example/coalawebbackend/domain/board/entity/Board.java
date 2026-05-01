package com.example.coalawebbackend.domain.board.entity;


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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "boards",
        uniqueConstraints = @UniqueConstraint(name = "uk_boards_name", columnNames = "name")
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private BoardType type = BoardType.NORMAL;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", length = 255)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.type == null) {
            this.type = BoardType.NORMAL;
        }
    }

    public void updateBoard(String name, String description, Boolean isActive) {
        this.name = name;
        this.description = description;
        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    public static Board createFromBoard(String name, String description, String type, User user) {
        BoardType resolvedType = (type == null || type.isBlank())
                ? BoardType.NORMAL
                : BoardType.valueOf(type.trim().toUpperCase());
        return Board.builder()
                .name(name)
                .description(description)
                .type(Objects.requireNonNullElse(resolvedType, BoardType.NORMAL))
                .user(user)
                .build();
    }
}
