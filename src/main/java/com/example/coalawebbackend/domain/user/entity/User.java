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
                @Index(name = "idx_users_grade", columnList = "grade")
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

    // 선택: 생년월일 (LocalDate 로 정규화)
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // 선택: 성별
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    // 필수: 소속 학과/학부
    @Column(name = "department", nullable = false, length = 100)
    private String department;

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
}
