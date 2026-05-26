package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitCommentRepository extends JpaRepository<RecruitComment, Long> {

    List<RecruitComment> findByRecruitPost_IdOrderByCreatedAtAsc(String recruitId);

    void deleteByRecruitPost_Id(String recruitId);
}
