package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitPostRepository extends JpaRepository<RecruitPost, String> {

    List<RecruitPost> findAllByOrderByCreatedAtDesc();

    List<RecruitPost> findByCategoryOrderByCreatedAtDesc(String category);
}
