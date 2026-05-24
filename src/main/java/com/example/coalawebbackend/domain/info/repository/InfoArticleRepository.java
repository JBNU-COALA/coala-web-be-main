package com.example.coalawebbackend.domain.info.repository;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InfoArticleRepository extends JpaRepository<InfoArticle, Long> {

    List<InfoArticle> findAllByOrderBySourceDateDescIdDesc();

    List<InfoArticle> findByCategoryOrderBySourceDateDescIdDesc(InfoCategory category);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM InfoArticle a
            WHERE a.content LIKE CONCAT('%', :needle, '%')
               OR a.imageUrl LIKE CONCAT('%', :needle, '%')
            """)
    boolean existsByContentOrImageReference(@Param("needle") String needle);
}
