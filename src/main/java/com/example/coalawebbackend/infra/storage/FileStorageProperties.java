package com.example.coalawebbackend.infra.storage;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private String rootPath = "/data/uploads";
    private long maxFileSize = 10 * 1024 * 1024L;
    private boolean markdownArchiveEnabled = true;
    private List<String> allowedImageTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private List<String> allowedAttachmentTypes = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );
}
