package com.example.coalawebbackend.domain.resource.service;

import com.example.coalawebbackend.api.resource.dto.CreateResourceRequest;
import com.example.coalawebbackend.api.resource.dto.ResourceResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.resource.entity.Resource;
import com.example.coalawebbackend.domain.resource.repository.ResourceRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional
    public ResourceResponse createResource(Post post, User user, CreateResourceRequest request) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        Resource resource = Resource.create(post, user,
                request.getFileName(), request.getFileUrl(),
                request.getFileType(), request.getFileSize());
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    public List<ResourceResponse> getResources(Post post) {
        return resourceRepository. findByPostWithFetch(post)
                .stream()
                .map(ResourceResponse::from)
                .toList();
    }

    @Transactional
    public void deleteResource(Long resourceId, User user) {
        Resource resource = getResourceById(resourceId);
        validateResourceOwner(resource, user);
        resourceRepository.delete(resource);
    }

    public Resource getResourceById(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void validateResourceOwner(Resource resource, User user) {
        if (!resource.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
