package com.example.coalawebbackend.domain.commentlike.repository;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.commentlike.entity.CommentLike;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cl FROM CommentLike cl WHERE cl.user = :user AND cl.comment = :comment")
    Optional<CommentLike> findByUserAndCommentWithLock(@Param("user") User user, @Param("comment") Comment comment);

    long countByComment(Comment comment);
}
