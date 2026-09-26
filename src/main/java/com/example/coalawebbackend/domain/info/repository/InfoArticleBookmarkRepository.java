package com.example.coalawebbackend.domain.info.repository;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoArticleBookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoArticleBookmarkRepository extends JpaRepository<InfoArticleBookmark, Long> {
    Optional<InfoArticleBookmark> findByUser_IdAndArticle_Id(Long userId, Long articleId);
    boolean existsByUser_IdAndArticle_Id(Long userId, Long articleId);
    long countByArticle_Id(Long articleId);
    @EntityGraph(attributePaths = {"article", "article.author"})
    List<InfoArticleBookmark> findByUser_IdOrderByCreatedAtDescIdDesc(Long userId);
    void deleteByArticle(InfoArticle article);
}
