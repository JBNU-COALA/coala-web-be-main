package com.example.coalawebbackend.api.site.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.Locale;

public record SiteBannerRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 80) String eyebrow,
        @Size(max = 2000) String description,
        @Size(max = 2000) String imageUrl,
        @NotBlank @Size(max = 500) String targetPath,
        @NotBlank @Size(max = 40) String actionLabel,
        @NotNull @Pattern(regexp = "green|blue|coral") String tone,
        @NotNull @PositiveOrZero Integer sortOrder,
        @NotNull Boolean enabled
) {
    @AssertTrue(message = "targetPath must be a safe root-relative URL")
    public boolean isTargetPathSafe() {
        return isInternalPath(targetPath);
    }

    @AssertTrue(message = "imageUrl must be HTTP(S) or a safe root-relative URL")
    public boolean isImageUrlSafe() {
        if (imageUrl == null || imageUrl.isBlank()) return true;
        if (isInternalPath(imageUrl)) return true;
        try {
            URI uri = URI.create(imageUrl);
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null
                    && !hasUnsafeCharacters(uri.toString());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isInternalPath(String value) {
        if (value == null || !value.startsWith("/") || value.startsWith("//") || hasUnsafeCharacters(value)) return false;
        try {
            URI uri = URI.create(value);
            String path = uri.getPath();
            return !uri.isAbsolute() && uri.getRawAuthority() == null && path != null
                    && path.startsWith("/") && !path.startsWith("//")
                    && !hasUnsafeCharacters(path) && !hasUnsafeCharacters(uri.getQuery())
                    && !hasUnsafeCharacters(uri.getFragment());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean hasUnsafeCharacters(String value) {
        if (value == null) return false;
        return value.chars().anyMatch(c -> c == '\\' || Character.isISOControl(c))
                || value.toLowerCase(Locale.ROOT).contains("%25");
    }
}
