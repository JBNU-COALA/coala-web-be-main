package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitPostRepository extends JpaRepository<RecruitPost, String> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from RecruitPost r where r.id = :id")
    java.util.Optional<RecruitPost> findForUpdate(@org.springframework.data.repository.query.Param("id") String id);

    List<RecruitPost> findAllByOrderByCreatedAtDesc();

    List<RecruitPost> findByCategoryOrderByCreatedAtDesc(String category);

    List<RecruitPost> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);
}
