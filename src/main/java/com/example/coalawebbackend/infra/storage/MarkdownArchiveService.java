package com.example.coalawebbackend.infra.storage;

import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.post.entity.Post;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownArchiveService {

    private static final DateTimeFormatter SNAPSHOT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final FileStorageProperties properties;

    public void savePostSnapshot(Post post) {
        if (post == null || post.getPostId() == null) {
            return;
        }
        saveSnapshot("posts", String.valueOf(post.getPostId()), post.getTitle(), post.getContent(),
                post.getCreatedAt(), post.getUpdatedAt());
    }

    public void saveInfoArticleSnapshot(InfoArticle article) {
        if (article == null || article.getId() == null) {
            return;
        }
        saveSnapshot("info-articles", String.valueOf(article.getId()), article.getTitle(), article.getContent(),
                article.getCreatedAt(), article.getUpdatedAt());
    }

    private void saveSnapshot(
            String category,
            String id,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (!properties.isMarkdownArchiveEnabled()) {
            return;
        }

        Path basePath = resolve(Path.of("markdown", category, id));
        Path currentPath = basePath.resolve("current.md");
        Path historyPath = basePath.resolve("history")
                .resolve(SNAPSHOT_FORMAT.format(LocalDateTime.now()) + ".md");
        String markdown = toMarkdown(category, id, title, content, createdAt, updatedAt);

        try {
            Files.createDirectories(historyPath.getParent());
            writeAtomically(currentPath, markdown);
            writeAtomically(historyPath, markdown);
        } catch (IOException e) {
            log.warn("Failed to archive markdown snapshot: category={}, id={}", category, id, e);
        }
    }

    private Path resolve(Path relativePath) {
        Path rootPath = Path.of(properties.getRootPath()).toAbsolutePath().normalize();
        Path resolved = rootPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Invalid markdown archive path");
        }
        return resolved;
    }

    private void writeAtomically(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Path tempPath = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        Files.writeString(tempPath, content, StandardCharsets.UTF_8);
        try {
            Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String toMarkdown(
            String category,
            String id,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return """
                <!--
                category: %s
                id: %s
                createdAt: %s
                updatedAt: %s
                -->

                # %s

                %s
                """.formatted(
                category,
                id,
                createdAt == null ? "" : createdAt,
                updatedAt == null ? "" : updatedAt,
                title == null ? "" : title,
                content == null ? "" : content
        );
    }
}
