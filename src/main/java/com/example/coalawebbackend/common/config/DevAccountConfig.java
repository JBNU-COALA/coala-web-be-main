package com.example.coalawebbackend.common.config;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!prod")
@RequiredArgsConstructor
public class DevAccountConfig {

    private static final String DEV_LOGIN_ID = "test";
    private static final String DEV_PASSWORD = "test1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.dev-account.enabled:true}")
    private boolean enabled;

    @Bean
    public ApplicationRunner devAccountRunner() {
        return args -> {
            if (!enabled || userRepository.existsByEmail(DEV_LOGIN_ID)) {
                return;
            }

            User devUser = User.builder()
                    .email(DEV_LOGIN_ID)
                    .password(passwordEncoder.encode(DEV_PASSWORD))
                    .name("코알라")
                    .nickname("coala-test")
                    .birthDate(LocalDate.of(2000, 1, 1))
                    .gender(Gender.PREFER_NOT_TO_SAY)
                    .department("컴퓨터인공지능학부")
                    .studentId("20180000")
                    .grade(4)
                    .githubId("coala-test")
                    .academicStatus(AcademicStatus.ENROLLED)
                    .build();

            userRepository.save(devUser);
        };
    }
}
