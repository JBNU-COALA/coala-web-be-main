package com.example.coalawebbackend.site;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.coalawebbackend.api.site.dto.SiteBannerRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SiteBannerValidationTest {
    private SiteBannerRequest request(String path, String image) {
        return new SiteBannerRequest("Title", "", "", image, path, "Open", "green", 0, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://evil.test", "//evil.test", "///evil.test", "/\\evil.test",
            "/%2fevil.test", "/%5cevil.test", "/%252fevil.test", "/%0aevil.test", "/ok?x=%0d",
            "javascript:alert(1)", "relative", " /about", "/bad path", "/bad%"})
    void rejectsExternalAndEncodedTargets(String path) {
        assertThat(request(path, "").isTargetPathSafe()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/about", "/community/board?sort=recent#posts", "/services/a%20b"})
    void acceptsInternalTargets(String path) {
        assertThat(request(path, "").isTargetPathSafe()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "data:image/svg+xml,x", "//evil.test/a.png", "https://user:pass@evil.test/a"})
    void rejectsUnsafeImages(String image) {
        assertThat(request("/about", image).isImageUrlSafe()).isFalse();
    }

    @Test
    void validatesRequiredFieldsAndBounds() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(request("/about", "/coala-developer.png"))).isEmpty();
            assertThat(validator.validate(request("/about", "https://example.test/image.png"))).isEmpty();
            assertThat(validator.validate(new SiteBannerRequest(" ", "x".repeat(81), "x".repeat(2001), "",
                    "/about", "x".repeat(41), "pink", -1, null))).hasSizeGreaterThanOrEqualTo(7);
        }
    }
}
