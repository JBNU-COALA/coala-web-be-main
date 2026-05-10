package com.example.coalawebbackend.api.attachment.controller;

import com.example.coalawebbackend.api.attachment.dto.AttachmentDownloadResponse;
import com.example.coalawebbackend.api.attachment.dto.AttachmentUploadResponse;
import com.example.coalawebbackend.domain.attachment.service.AttachmentService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final UserService userService;

    @PostMapping("/images")
    public ResponseEntity<AttachmentUploadResponse> uploadImage(
            @AuthenticationPrincipal String userId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findById(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.uploadImage(user, file));
    }

    @PostMapping("/files")
    public ResponseEntity<AttachmentUploadResponse> uploadFile(
            @AuthenticationPrincipal String userId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.findById(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.uploadFile(user, file));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<?> download(@PathVariable Long attachmentId) {
        AttachmentDownloadResponse response = attachmentService.getDownload(attachmentId);
        String encodedName = URLEncoder.encode(response.originalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .contentLength(response.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(encodedName, StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(response.resource());
    }
}
