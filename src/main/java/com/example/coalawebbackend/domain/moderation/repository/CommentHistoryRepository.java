package com.example.coalawebbackend.domain.moderation.repository;

import com.example.coalawebbackend.domain.moderation.entity.CommentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentHistoryRepository extends JpaRepository<CommentHistory, Long> {
}
