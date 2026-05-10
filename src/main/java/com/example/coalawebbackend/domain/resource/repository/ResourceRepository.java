package com.example.coalawebbackend.domain.resource.repository;

import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.resource.entity.Resource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("SELECT r FROM Resource r " +
            "JOIN FETCH r.user " +
            "JOIN FETCH r.post " +
            "WHERE r.post = :post")
    List<Resource> findByPostWithFetch(@Param("post") Post post);

    void deleteByPost(Post post);
}
