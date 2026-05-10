package com.example.coalawebbackend.domain.instance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InstanceAttachedFile {

    @Column(name = "file_name", nullable = false, length = 255)
    private String name;

    @Column(name = "file_size", nullable = false, length = 50)
    private String size;

    @Column(name = "uploaded_at", nullable = false, length = 20)
    private String uploadedAt;
}
