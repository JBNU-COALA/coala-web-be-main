package com.example.coalawebbackend.api.user.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(example = "P@ssw0rd!")
    private String password;

    @NotBlank
    @Schema(example = "홍길동")
    private String name;

    @Schema(example = "길동이")
    private String nickname;

    @Schema(example = "2000-01-01")
    private String birthDate;

    @Schema(example = "MALE", description = "MALE / FEMALE / NONE")
    private Gender gender;

    @NotBlank
    @Schema(example = "컴퓨터공학과")
    private String department;

    @NotBlank
    @Schema(example = "202012345")
    private String studentId;

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
