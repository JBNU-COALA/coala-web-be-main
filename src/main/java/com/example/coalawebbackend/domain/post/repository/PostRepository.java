package com.example.coalawebbackend.domain.post.repository;

import com.example.coalawebbackend.domain.post.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByBoardBoardId(Long boardId);
}
