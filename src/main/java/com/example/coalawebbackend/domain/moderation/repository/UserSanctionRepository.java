package com.example.coalawebbackend.domain.moderation.repository;

import com.example.coalawebbackend.domain.moderation.entity.UserSanction;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {

    @Query("""
            SELECT s
            FROM UserSanction s
            WHERE s.user = :user
              AND s.startAt <= :now
              AND (s.endAt IS NULL OR s.endAt > :now)
            """)
    List<UserSanction> findActive(@Param("user") User user, @Param("now") LocalDateTime now);
}
