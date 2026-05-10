package com.example.coalawebbackend.infra.storage;

public record StoredFile(
        String originalName,
        String storedName,
        String storagePath,
        String contentType,
        long fileSize,
        String extension,
        String checksum
) {
}
