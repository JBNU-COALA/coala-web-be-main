package com.example.coalawebbackend.domain.postlike.repository;

import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.postlike.entity.PostLike;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pl FROM PostLike pl WHERE pl.user = :user AND pl.post = :post")
    Optional<PostLike> findByUserAndPostWithLock(@Param("user") User user, @Param("post") Post post);

    long countByPost(Post post);

    boolean existsByUser_IdAndPost_PostId(Long userId, Long postId);

    void deleteByPost(Post post);
}
