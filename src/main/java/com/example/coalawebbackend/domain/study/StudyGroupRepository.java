package com.example.coalawebbackend.domain.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Optional<StudyGroup> findByRecruit_Id(String recruitId);
    boolean existsByRecruit_Id(String recruitId);
    @EntityGraph(attributePaths = {"recruit", "recruit.author"})
    List<StudyGroup> findAllByOrderByIdDesc();
}
