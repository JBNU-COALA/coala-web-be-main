package com.example.coalawebbackend.domain.user.repository;

import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @org.springframework.data.jpa.repository.Query("""
            select u from User u where u.verified = true
            and (lower(u.name) like lower(concat('%', :query, '%'))
                 or lower(u.githubId) like lower(concat('%', :query, '%')))
            """)
    List<User> searchActivityMembers(@org.springframework.data.repository.query.Param("query") String query,
                                    org.springframework.data.domain.Pageable pageable);

    Optional<User> findByEmail(String email);

    List<User> findByVerifiedTrue();

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByGithubId(String githubId);

    boolean existsByLinkedinUrl(String linkedinUrl);

    boolean existsByProfileCustomizationContaining(String needle);
}
