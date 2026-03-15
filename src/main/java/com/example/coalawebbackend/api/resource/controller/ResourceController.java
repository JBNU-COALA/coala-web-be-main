package com.example.coalawebbackend.api.resource.controller;

import com.example.coalawebbackend.api.resource.dto.CreateResourceRequest;
import com.example.coalawebbackend.api.resource.dto.ResourceResponse;
import com.example.coalawebbackend.api.resource.facade.ResourceFacade;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/resources")
public class ResourceController implements ResourceControllerSpec {

    private final ResourceFacade resourceFacade;

    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(
            @PathVariable Long postId,
            @Valid @RequestBody CreateResourceRequest request,
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceFacade.createResource(postId, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getResources(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(resourceFacade.getResources(postId));
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal String userId
    ) {
        resourceFacade.deleteResource(resourceId, userId);
        return ResponseEntity.noContent().build();
    }
}
