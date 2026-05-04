package com.example.coalawebbackend.domain.user.repository;

import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByGithubId(String githubId);

    boolean existsByLinkedinUrl(String linkedinUrl);
}
