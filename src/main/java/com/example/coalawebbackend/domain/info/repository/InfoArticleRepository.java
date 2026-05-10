package com.example.coalawebbackend.domain.info.repository;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoArticleRepository extends JpaRepository<InfoArticle, Long> {

    List<InfoArticle> findAllByOrderBySourceDateDescIdDesc();

    List<InfoArticle> findByCategoryOrderBySourceDateDescIdDesc(InfoCategory category);
}
