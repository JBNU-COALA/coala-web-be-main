package com.example.coalawebbackend.domain.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Optional<StudyGroup> findByRecruit_Id(String recruitId);
    boolean existsByRecruit_Id(String recruitId);
    @EntityGraph(attributePaths = {"recruit", "recruit.author"})
    List<StudyGroup> findAllByOrderByIdDesc();

    @Query("""
            select count(g) from StudyGroup g
            where g.recruit.author.id = :userId or exists (
                select a.id from RecruitApplication a
                where a.recruitPost = g.recruit and a.user.id = :userId and a.status = 'accepted'
            )
            """)
    long countForMember(@Param("userId") Long userId);
}
