package com.example.coalawebbackend.api.user.facade;

import com.example.coalawebbackend.api.auth.dto.EmailVerificationResponse;
import com.example.coalawebbackend.api.auth.service.EmailVerificationService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFacade {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public EmailVerificationResponse createUser(User user) {
        User withEncodedPassword =
                User.builder()
                        .email(user.getEmail())
                        .password(passwordEncoder.encode(user.getPassword()))
                        .name(user.getName())
                        .nickname(user.getNickname())
                        .birthDate(user.getBirthDate())
                        .gender(user.getGender())
                        .department(user.getDepartment())
                        .studentId(user.getStudentId())
                        .grade(user.getGrade())
                        .githubId(user.getGithubId())
                        .linkedinUrl(user.getLinkedinUrl())
                        .academicStatus(user.getAcademicStatus())
                        .verified(false)
                        .build();
        User createdUser = userService.createUser(withEncodedPassword);

        emailVerificationService.issue(createdUser);
        return EmailVerificationResponse.from(createdUser, "인증 메일을 보냈습니다.");
    }
}
