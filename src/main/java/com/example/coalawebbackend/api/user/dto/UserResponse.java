package com.example.coalawebbackend.api.user.dto;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
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
    private String birthDate;
    private Gender gender;
    private String department;
    private String studentId;
    private Integer grade;
    private AcademicStatus academicStatus;
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
                .studentId(user.getStudentId())
                .grade(user.getGrade())
                .academicStatus(user.getAcademicStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
