package com.example.coalawebbackend.domain.attachment.entity;

public enum FileCategory {
    IMAGE("images"),
    ATTACHMENT("attachments"),
    THUMBNAIL("thumbnails"),
    PROFILE("profiles");

    private final String directoryName;

    FileCategory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String directoryName() {
        return directoryName;
    }
}
