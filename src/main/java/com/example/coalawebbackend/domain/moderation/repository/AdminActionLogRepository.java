package com.example.coalawebbackend.domain.moderation.repository;

import com.example.coalawebbackend.domain.moderation.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
}
