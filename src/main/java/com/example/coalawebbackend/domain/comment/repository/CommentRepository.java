package com.example.coalawebbackend.domain.comment.repository;

import com.example.coalawebbackend.domain.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    List<Comment> findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<Comment> findByPost_PostIdAndParent_IdOrderByCreatedAtAsc(Long postId, Long parentId);

    long countByPost_PostId(Long postId);
}
