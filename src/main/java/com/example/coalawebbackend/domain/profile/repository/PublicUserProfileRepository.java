package com.example.coalawebbackend.domain.profile.repository;

import com.example.coalawebbackend.domain.profile.entity.PublicUserProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicUserProfileRepository extends JpaRepository<PublicUserProfile, Long> {

    boolean existsByGithubHandle(String githubHandle);

    List<PublicUserProfile> findAllByOrderByIdAsc();
}
