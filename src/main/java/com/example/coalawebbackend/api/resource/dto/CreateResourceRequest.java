package com.example.coalawebbackend.api.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "리소스 생성 요청 DTO")
public class CreateResourceRequest {

    @Schema(description = "파일 이름", example = "algorithm.pdf")
    @NotBlank(message = "파일 이름은 필수입니다.")
    @Size(max = 255)
    private String fileName;

    @Schema(description = "파일 URL", example = "https://example.com/file/algorithm.pdf")
    @NotBlank(message = "파일 URL은 필수입니다.")
    @Size(max = 500)
    @Pattern(regexp = "^https://.+", message = "파일 URL은 https URL이어야 합니다.")
    private String fileUrl;

    @Schema(description = "파일 타입", example = "PDF")
    @NotBlank(message = "파일 타입은 필수입니다.")
    @Size(max = 50)
    private String fileType;

    @Schema(description = "파일 크기 (byte)", example = "102400")
    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    private Long fileSize;
}
