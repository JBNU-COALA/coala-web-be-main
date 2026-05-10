package com.example.coalawebbackend.infra.storage;

import com.example.coalawebbackend.domain.attachment.entity.FileCategory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    StoredFile store(MultipartFile file, FileCategory category);

    Resource load(String storagePath);

    void delete(String storagePath);

    boolean exists(String storagePath);
}
