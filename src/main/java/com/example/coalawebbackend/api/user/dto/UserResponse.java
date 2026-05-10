package com.example.coalawebbackend.api.user.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private Gender gender;
    private String department;
    private String lab;
    private String studentId;
    private Integer grade;
    private String githubId;
    private String linkedinUrl;
    private AcademicStatus academicStatus;
    private boolean verified;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .department(user.getDepartment())
                .lab(user.getLab())
                .studentId(user.getStudentId())
                .grade(user.getGrade())
                .githubId(user.getGithubId())
                .linkedinUrl(user.getLinkedinUrl())
                .academicStatus(user.getAcademicStatus())
                .verified(user.isVerified())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
