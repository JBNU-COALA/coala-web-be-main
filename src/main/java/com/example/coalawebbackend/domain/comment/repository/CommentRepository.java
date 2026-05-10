package com.example.coalawebbackend.domain.comment.repository;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import com.example.coalawebbackend.domain.comment.entity.CommentStatus;
import com.example.coalawebbackend.domain.post.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.post.postId = :postId
              AND c.parent IS NULL
              AND c.status IN :statuses
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findVisibleParents(@Param("postId") Long postId, @Param("statuses") List<CommentStatus> statuses);

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.post.postId = :postId
              AND c.parent.id = :parentId
              AND c.status IN :statuses
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findVisibleReplies(
            @Param("postId") Long postId,
            @Param("parentId") Long parentId,
            @Param("statuses") List<CommentStatus> statuses);

    long countByPost_PostId(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteByPost(@Param("post") Post post);
}
