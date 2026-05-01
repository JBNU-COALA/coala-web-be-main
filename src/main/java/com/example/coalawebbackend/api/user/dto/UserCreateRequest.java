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
    @Size(max = 100)
    @Schema(example = "user@example.com")
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

    @Schema(example = "MALE", description = "MALE / FEMALE / OTHER / PREFER_NOT_TO_SAY")
    private Gender gender;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "컴퓨터공학과")
    private String department;

    @NotBlank
    @Pattern(regexp = "\\d{4,20}", message = "학번은 4~20자리 숫자여야 합니다.")
    @Schema(example = "202012345")
    private String studentId;

    @Min(1)
    @Max(6)
    @Schema(example = "3")
    private Integer grade;

    @NotNull
    @Schema(example = "ENROLLED", description = "ENROLLED / ON_LEAVE / GRADUATED")
    private AcademicStatus academicStatus;

    public User toEntity() {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .nickname(nickname)
                .birthDate(birthDate)
                .gender(gender)
                .department(department)
                .studentId(studentId)
                .grade(grade)
                .academicStatus(academicStatus)
                .build();
    }
}
