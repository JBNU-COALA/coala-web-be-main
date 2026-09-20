package com.example.coalawebbackend.domain.study;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, String> {
    @EntityGraph(attributePaths = {"group", "group.recruit", "group.recruit.author", "attendance", "attendance.user"})
    List<StudyRecord> findByDateBetweenOrderByDateDescUpdatedAtDesc(LocalDate from, LocalDate to);
}
