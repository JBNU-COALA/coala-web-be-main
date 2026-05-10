package com.example.coalawebbackend.api.user.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank
    @Email
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@jbnu\\.ac\\.kr$",
            message = "전북대학교 이메일(@jbnu.ac.kr)만 사용할 수 있습니다.")
    @Size(max = 100)
    @Schema(example = "user@jbnu.ac.kr")
    private String email;

    @NotBlank
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Schema(example = "P@ssw0rd!")
    private String password;

    @NotBlank
    @Size(max = 50)
    @Schema(example = "홍길동")
    private String name;

    @Size(max = 50)
    @Schema(example = "길동이")
    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2000-01-01", type = "string", format = "date")
    private LocalDate birthDate;

    @NotNull
    @Schema(example = "MALE", description = "MALE / FEMALE / OTHER / PREFER_NOT_TO_SAY")
    private Gender gender;

    @Size(max = 100)
    @Schema(example = "컴퓨터공학과", description = "기존 호환용 선택 필드")
    private String department;

    @NotBlank
    @Pattern(regexp = "\\d{4,20}", message = "학번은 4~20자리 숫자여야 합니다.")
    @Schema(example = "202012345")
    private String studentId;

    @NotNull
    @Min(1)
    @Max(6)
    @Schema(example = "3")
    private Integer grade;

    @NotBlank
    @Size(max = 39)
    @Pattern(
            regexp = "^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$",
            message = "GitHub 아이디 형식이 올바르지 않습니다.")
    @Schema(example = "coala-dev")
    private String githubId;

    @Size(max = 255)
    @Pattern(
            regexp = "^$|^https://(www\\.)?linkedin\\.com/in/[A-Za-z0-9_-]+/?$",
            message = "LinkedIn 프로필 URL 형식이 올바르지 않습니다.")
    @Schema(example = "https://www.linkedin.com/in/coala-dev")
    private String linkedinUrl;

    @NotNull
    @Schema(example = "ENROLLED", description = "PROFESSOR / ASSISTANT / ENROLLED / ON_LEAVE / GRADUATED / GENERAL")
    private AcademicStatus academicStatus;

    public User toEntity() {
        return User.builder()
                .email(normalizeEmail())
                .password(password)
                .name(name)
                .nickname(nickname)
                .birthDate(birthDate)
                .gender(gender)
                .department(normalizeDepartment())
                .studentId(studentId)
                .grade(grade)
                .githubId(githubId.trim())
                .linkedinUrl(normalizeOptional(linkedinUrl))
                .academicStatus(academicStatus)
                .build();
    }

    private String normalizeDepartment() {
        return department == null || department.isBlank()
                ? "미입력"
                : department.trim();
    }

    private String normalizeEmail() {
        return email.trim().toLowerCase();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
