package com.example.coalawebbackend.domain.infolike.repository;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.infolike.entity.InfoArticleLike;
import com.example.coalawebbackend.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InfoArticleLikeRepository extends JpaRepository<InfoArticleLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT il FROM InfoArticleLike il WHERE il.user = :user AND il.article = :article")
    Optional<InfoArticleLike> findByUserAndArticleWithLock(
            @Param("user") User user,
            @Param("article") InfoArticle article);

    long countByArticle(InfoArticle article);

    boolean existsByUser_IdAndArticle_Id(Long userId, Long articleId);

    void deleteByArticle(InfoArticle article);
}
