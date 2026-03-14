package com.example.coalawebbackend.api.resource.facade;

import com.example.coalawebbackend.api.resource.dto.CreateResourceRequest;
import com.example.coalawebbackend.api.resource.dto.ResourceResponse;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.service.PostService;
import com.example.coalawebbackend.domain.resource.service.ResourceService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceFacade {

    private final ResourceService resourceService;
    private final PostService postService;
    private final UserService userService;

    public ResourceResponse createResource(Long postId, CreateResourceRequest request, String userId) {
        User user = userService.findById(userId);
        Post post = postService.getPostById(postId);

        return resourceService.createResource(post, user, request);
    }

    public List<ResourceResponse> getResources(Long postId) {
        Post post = postService.getPostById(postId);
        return resourceService.getResources(post);
    }

    public void deleteResource(Long resourceId, String userId) {
        User user = userService.findById(userId);
        resourceService.deleteResource(resourceId, user);
    }
}
