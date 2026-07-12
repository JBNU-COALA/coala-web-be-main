package com.example.coalawebbackend.domain.anonymous.repository;

import com.example.coalawebbackend.domain.anonymous.entity.AnonymousProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousProfileRepository extends JpaRepository<AnonymousProfile, Long> {

    Optional<AnonymousProfile> findByBoard_BoardIdAndUser_Id(Long boardId, Long userId);
}
