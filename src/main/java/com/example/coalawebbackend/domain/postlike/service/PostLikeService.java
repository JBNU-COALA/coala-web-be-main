package com.example.coalawebbackend.domain.postlike.service;

import com.example.coalawebbackend.api.postlike.dto.PostLikeResponse;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.postlike.entity.PostLike;
import com.example.coalawebbackend.domain.postlike.repository.PostLikeRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostLikeResponse toggleLike(User user, Post post) {
        Optional<PostLike> existing = postLikeRepository.findByUserAndPostWithLock(user, post);

        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            long likeCount = postLikeRepository.countByPost(post);
            return PostLikeResponse.of(false, likeCount);
        }

        postLikeRepository.save(PostLike.create(user, post));
        long likeCount = postLikeRepository.countByPost(post);
        return PostLikeResponse.of(true, likeCount);
    }
}
