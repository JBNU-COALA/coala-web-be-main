package com.example.coalawebbackend.api.admin.controller;

import com.example.coalawebbackend.api.post.dto.PostListResponse;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PermissionService permissionService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostListResponse>> getPosts(
            @AuthenticationPrincipal String adminId,
            @RequestParam(required = false) PostStatus status
    ) {
        permissionService.assertModerator(userService.findById(adminId));
        List<PostListResponse> posts = (status == null
                        ? postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                        : postRepository.findByStatusOrderByCreatedAtDesc(status))
                .stream()
                .map(PostListResponse::from)
                .toList();
        return ResponseEntity.ok(posts);
    }
}
