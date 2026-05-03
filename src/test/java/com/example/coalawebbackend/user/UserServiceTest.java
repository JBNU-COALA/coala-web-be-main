package com.example.coalawebbackend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.Gender;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.example.coalawebbackend.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User makeUser() {
        return User.builder()
                .email("member@example.com")
                .password("encoded-password")
                .name("홍길동")
                .gender(Gender.MALE)
                .department("미입력")
                .studentId("202012345")
                .grade(3)
                .githubId("coala-dev")
                .linkedinUrl("https://www.linkedin.com/in/coala-dev")
                .academicStatus(AcademicStatus.ENROLLED)
                .build();
    }

    @Test
    @DisplayName("회원 생성 성공")
    void createUser_success() {
        User user = makeUser();

        given(userRepository.save(user)).willReturn(user);

        User created = userService.createUser(user);

        assertThat(created).isEqualTo(user);
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 회원 생성을 막는다")
    void createUser_duplicateEmail() {
        User user = makeUser();
        given(userRepository.existsByEmail(user.getEmail())).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));

        then(userRepository).should(never()).save(user);
    }

    @Test
    @DisplayName("이미 등록된 GitHub 아이디면 회원 생성을 막는다")
    void createUser_duplicateGithubId() {
        User user = makeUser();
        given(userRepository.existsByGithubId(user.getGithubId())).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_GITHUB_ID));

        then(userRepository).should(never()).save(user);
    }
}
