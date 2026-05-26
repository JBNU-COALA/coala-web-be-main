package com.example.coalawebbackend.domain.recruit.repository;

import com.example.coalawebbackend.domain.recruit.entity.RecruitBookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitBookmarkRepository extends JpaRepository<RecruitBookmark, Long> {

    Optional<RecruitBookmark> findByRecruitPost_IdAndUser_Id(String recruitId, Long userId);

    List<RecruitBookmark> findByRecruitPost_Id(String recruitId);

    void deleteByRecruitPost_Id(String recruitId);
}
