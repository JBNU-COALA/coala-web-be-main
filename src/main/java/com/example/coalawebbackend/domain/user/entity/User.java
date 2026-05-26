package com.example.coalawebbackend.domain.user.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_student_id", columnNames = "student_id"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname"),
                @UniqueConstraint(name = "uk_users_github_id", columnNames = "github_id"),
                @UniqueConstraint(name = "uk_users_linkedin_url", columnNames = "linkedin_url")
        },
        indexes = {
                @Index(name = "idx_users_academic_status", columnList = "academic_status"),
                @Index(name = "idx_users_grade", columnList = "grade"),
                @Index(name = "idx_users_lab", columnList = "lab"),
                @Index(name = "idx_users_role", columnList = "role")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 필수: 이메일 (로그인 아이디)
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    // 필수: 비밀번호 (BCrypt 해시 = 60자, 향후 알고리즘 변경 대비 100)
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    // 필수: 실명
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // 선택: 닉네임 (unique)
    @Column(name = "nickname", length = 50)
    private String nickname;

    // 선택: 백준 아이디
    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    // 필수: GitHub 아이디
    @Column(name = "github_id", nullable = false, length = 39)
    private String githubId;

    // 선택: LinkedIn 프로필 URL
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "profile_bio", length = 1000)
    private String profileBio;

    @Column(name = "profile_activity_note", columnDefinition = "TEXT")
    private String profileActivityNote;

    @Column(name = "profile_award_note", columnDefinition = "TEXT")
    private String profileAwardNote;

    @Column(name = "profile_shared_repositories", length = 1000)
    private String profileSharedRepositories;

    @Column(name = "profile_customization", columnDefinition = "TEXT")
    private String profileCustomization;

    // 선택: 생년월일 (LocalDate 로 정규화)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // 선택: 성별
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    // 필수: 소속 학과/학부
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    // 선택: 소속 연구실
    @Column(name = "lab", length = 150)
    private String lab;

    // 필수: 학번 (unique)
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    // 선택: 학년 (1~6: 학부 + 대학원 고려)
    @Column(name = "grade")
    private Integer grade;

    // 필수: 학적 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false, length = 20)
    private AcademicStatus academicStatus;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30)
    private UserRole role = UserRole.USER;

    public void markVerified() {
        this.verified = true;
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }

    public UserRole getRole() {
        return role == null ? UserRole.USER : role;
    }

    public void grantRole(UserRole role) {
        this.role = role == null ? UserRole.USER : role;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(
            String bio,
            String activityNote,
            String awardNote,
            String sharedRepositories,
            String customization
    ) {
        this.profileBio = normalizeBlank(bio);
        this.profileActivityNote = normalizeBlank(activityNote);
        this.profileAwardNote = normalizeBlank(awardNote);
        this.profileSharedRepositories = normalizeBlank(sharedRepositories);
        this.profileCustomization = normalizeBlank(customization);
    }

    public void updateAccountProfile(
            String email,
            String name,
            String studentId,
            String githubId,
            String lab,
            Gender gender,
            AcademicStatus academicStatus,
            String linkedinUrl
    ) {
        this.email = requireValue(email, this.email);
        this.name = requireValue(name, this.name);
        this.studentId = requireValue(studentId, this.studentId);
        this.githubId = requireValue(githubId, this.githubId);
        this.lab = normalizeBlank(lab);
        this.gender = gender == null ? this.gender : gender;
        this.academicStatus = academicStatus == null ? this.academicStatus : academicStatus;
        this.linkedinUrl = normalizeBlank(linkedinUrl);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String requireValue(String value, String fallback) {
        String normalized = normalizeBlank(value);
        return normalized == null ? fallback : normalized;
    }

    public void syncSeedAccount(
            String password,
            String name,
            String nickname,
            LocalDate birthDate,
            Gender gender,
            String department,
            String lab,
            String studentId,
            Integer grade,
            String githubId,
            AcademicStatus academicStatus) {
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
        this.department = department;
        this.lab = lab;
        this.studentId = studentId;
        this.grade = grade;
        this.githubId = githubId;
        this.academicStatus = academicStatus;
        this.verified = true;
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }
}
