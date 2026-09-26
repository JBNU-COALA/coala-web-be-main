package com.example.coalawebbackend.domain.study;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, String> {
    @EntityGraph(attributePaths = {"group", "group.recruit", "group.recruit.author", "attendance", "attendance.user"})
    List<StudyRecord> findByDateBetweenOrderByDateDescUpdatedAtDesc(LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"author", "group", "group.recruit", "group.recruit.author", "attendance", "attendance.user"})
    @Query("""
            select r from StudyRecord r
            where r.date between :from and :to
              and (:groupId is null or r.group.id = :groupId)
              and (:userId is null or r.author.id = :userId or exists (
                  select participant.id from StudyRecord participant join participant.attendance a
                  where participant.id = r.id and a.user.id = :userId
              ))
            order by r.date desc, r.updatedAt desc, r.id
            """)
    List<StudyRecord> findInRange(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                @Param("groupId") Long groupId, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"author", "group"})
    @Query("""
            select r from StudyRecord r
            where r.author.id = :userId or exists (
                select participant.id from StudyRecord participant join participant.attendance a
                where participant.id = r.id and a.user.id = :userId
            )
            order by r.date desc, r.updatedAt desc, r.id
            """)
    List<StudyRecord> findForMember(@Param("userId") Long userId);
}
