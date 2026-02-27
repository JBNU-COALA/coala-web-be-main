package com.example.coalawebbackend.api.user.facade;

import com.example.coalawebbackend.api.auth.dto.TokenInfo;
import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import com.example.coalawebbackend.api.user.dto.UserResponse;
import com.example.coalawebbackend.common.jwt.JwtTokenProvider;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFacade {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse createUser(User user) {
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
                        .academicStatus(user.getAcademicStatus())
                        .build();
        User createdUser = userService.createUser(withEncodedPassword);

        TokenInfo tokens =
                jwtTokenProvider.generateTokenInfo(
                        String.valueOf(createdUser.getId()), Map.of("email", createdUser.getEmail()));
        return new TokenResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                TOKEN_TYPE,
                UserResponse.from(createdUser));
    }
}
