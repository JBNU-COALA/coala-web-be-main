package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitApplicationRepository extends JpaRepository<RecruitApplication, Long> {

    List<RecruitApplication> findByUser_IdOrderBySubmittedAtDesc(Long userId);

    List<RecruitApplication> findByRecruitPost_IdOrderBySubmittedAtDesc(String recruitId);
}
