package com.example.coalawebbackend.domain.moderation.repository;

import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.entity.Report;
import com.example.coalawebbackend.domain.moderation.entity.ReportStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ModerationTargetType targetType, Long targetId);

    long countDistinctReporterByTargetTypeAndTargetIdAndStatusIn(
            ModerationTargetType targetType,
            Long targetId,
            List<ReportStatus> statuses);

    List<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status);
}
