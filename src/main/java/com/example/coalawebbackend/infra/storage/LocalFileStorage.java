package com.example.coalawebbackend.infra.storage;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.attachment.entity.FileCategory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage implements FileStorage {

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "sh", "bat", "cmd", "jsp", "php", "html", "js", "svg"
    );

    private final FileStorageProperties properties;
    private final Path rootPath;

    public LocalFileStorage(FileStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Path.of(properties.getRootPath()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file, FileCategory category) {
        validate(file, category);
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String storedName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        LocalDate today = LocalDate.now();
        String storagePath = Path.of(
                category.directoryName(),
                String.valueOf(today.getYear()),
                "%02d".formatted(today.getMonthValue()),
                "%02d".formatted(today.getDayOfMonth()),
                storedName
        ).toString().replace('\\', '/');
        Path destination = resolve(storagePath);

        try {
            Files.createDirectories(destination.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, destination);
            }
            return new StoredFile(
                    originalName,
                    storedName,
                    storagePath,
                    contentTypeOf(file),
                    file.getSize(),
                    extension,
                    HexFormat.of().formatHex(digest.digest())
            );
        } catch (IOException | NoSuchAlgorithmException e) {
            delete(storagePath);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public Resource load(String storagePath) {
        return new PathResource(resolve(storagePath));
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolve(storagePath));
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean exists(String storagePath) {
        return Files.isRegularFile(resolve(storagePath));
    }

    private void validate(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }

        String contentType = contentTypeOf(file);
        boolean imageCategory = category == FileCategory.IMAGE
                || category == FileCategory.THUMBNAIL
                || category == FileCategory.PROFILE;
        if (imageCategory && !properties.getAllowedImageTypes().contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }
        if (!imageCategory && !properties.getAllowedAttachmentTypes().contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }

        String extension = extensionOf(sanitizeOriginalName(file.getOriginalFilename()));
        if (!extension.isBlank() && BLOCKED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }
    }

    private Path resolve(String storagePath) {
        Path resolved = rootPath.resolve(storagePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }
        return resolved;
    }

    private String sanitizeOriginalName(String originalName) {
        String cleaned = StringUtils.cleanPath(originalName == null ? "file" : originalName);
        cleaned = cleaned.replace("\\", "/");
        int slashIndex = cleaned.lastIndexOf('/');
        return slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
    }

    private String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String contentTypeOf(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType.toLowerCase(Locale.ROOT);
    }
}
