package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitApplicationRepository extends JpaRepository<RecruitApplication, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "recruitPost"})
    List<RecruitApplication> findByRecruitPost_IdInAndStatus(List<String> recruitIds, String status);

    List<RecruitApplication> findByUser_IdOrderBySubmittedAtDesc(Long userId);

    Optional<RecruitApplication> findFirstByRecruitPost_IdAndUser_IdOrderBySubmittedAtDesc(String recruitId, Long userId);

    List<RecruitApplication> findByRecruitPost_IdOrderBySubmittedAtDesc(String recruitId);

    void deleteByRecruitPost_Id(String recruitId);
}
