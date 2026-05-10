package com.example.coalawebbackend.common.config;

import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DevAccountConfig {

    private static final String DEV_EMAIL = "test@test.com";
    private static final String DEV_PASSWORD = "test1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.dev-account.enabled:false}")
    private boolean enabled;

    @Bean
    public ApplicationRunner devAccountRunner() {
        return args -> {
            if (!enabled) {
                return;
            }

            String encodedPassword = passwordEncoder.encode(DEV_PASSWORD);
            User devUser = userRepository
                    .findByEmail(DEV_EMAIL)
                    .map(user -> {
                        user.syncSeedAccount(
                                encodedPassword,
                                "코알라",
                                "coala-test-2018",
                                LocalDate.of(2000, 1, 1),
                                null,
                                "컴퓨터인공지능학부",
                                "코알라",
                                "20180001",
                                4,
                                "coala-test-2018",
                                AcademicStatus.ENROLLED);
                        return user;
                    })
                    .orElseGet(() -> User.builder()
                            .email(DEV_EMAIL)
                            .password(encodedPassword)
                            .name("코알라")
                            .nickname("coala-test-2018")
                            .birthDate(LocalDate.of(2000, 1, 1))
                            .gender(null)
                            .department("컴퓨터인공지능학부")
                            .lab("코알라")
                            .studentId("20180001")
                            .grade(4)
                            .githubId("coala-test-2018")
                            .academicStatus(AcademicStatus.ENROLLED)
                            .verified(true)
                            .build());

            userRepository.save(devUser);
        };
    }
}
