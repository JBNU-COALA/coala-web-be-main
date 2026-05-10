package com.example.coalawebbackend.domain.moderation.repository;

import com.example.coalawebbackend.domain.moderation.entity.PostHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {
}
