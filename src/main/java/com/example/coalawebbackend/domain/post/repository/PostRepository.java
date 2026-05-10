package com.example.coalawebbackend.domain.post.repository;

import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByBoardBoardIdAndStatusOrderByCreatedAtDesc(Long boardId, PostStatus status);

    List<Post> findByStatusOrderByCreatedAtDesc(PostStatus status);

    Optional<Post> findByPostIdAndStatus(Long postId, PostStatus status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void increaseViewCount(@Param("postId") Long postId);
}
