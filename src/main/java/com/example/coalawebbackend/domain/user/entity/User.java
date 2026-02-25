package com.example.coalawebbackend.domain.user.entity;

import com.example.coalawebbackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필수: 이메일 (로그인 아이디)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 필수: 비밀번호
    @Column(nullable = false, length = 255)
    private String password;

    // 필수: 실명
    @Column(nullable = false, length = 50)
    private String name;

    // 선택: 닉네임
    @Column(length = 50)
    private String nickname;

    // 선택: 생년월일 (예: "1999-01-01")
    @Column(length = 10)
    private String birthDate;

    // 선택: 성별
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    // 필수: 소속 학과/학부
    @Column(nullable = false, length = 100)
    private String department;

    // 필수: 학번
    @Column(nullable = false, length = 20)
    private String studentId;

    // 선택: 학년 (1~4)
    private Integer grade;

    // 필수: 학적 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicStatus academicStatus;
}
