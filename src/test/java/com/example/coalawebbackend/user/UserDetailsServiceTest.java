package com.example.coalawebbackend.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.coalawebbackend.api.users.dto.UserDetailsRequest;
import com.example.coalawebbackend.api.users.dto.UserProfileRequest;
import com.example.coalawebbackend.api.users.service.UserDetailsService;
import com.example.coalawebbackend.api.users.service.UserDirectoryService;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.service.*;
import com.example.coalawebbackend.domain.user.entity.*;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserDetailsServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AdminAuditService audit = mock(AdminAuditService.class);
    private final UserDetailsService service = new UserDetailsService(users,
            new PermissionService(mock(SanctionPolicyService.class)), audit);

    private User user(long id, UserRole role) {
        return User.builder().id(id).email("member@example.com").password("hash")
                .name("Member").nickname("old").studentId("20200001").githubId("old-id")
                .department("Computing").academicStatus(AcademicStatus.ENROLLED)
                .role(role).verified(true).build();
    }

    private UserDetailsRequest details() {
        return new UserDetailsRequest("Changed", "", LocalDate.of(2000, 1, 2),
                Gender.PREFER_NOT_TO_SAY, "Engineering", "", "20200002", 4,
                "new-id", "new_boj", "", AcademicStatus.GRADUATED);
    }

    @Test void updatesAllEditableFieldsWithoutChangingIdentityOrPrivileges() {
        User target = user(2, UserRole.USER);
        service.apply(target, details());
        assertThat(target.getName()).isEqualTo("Changed");
        assertThat(target.getNickname()).isNull();
        assertThat(target.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 2));
        assertThat(target.getDepartment()).isEqualTo("Engineering");
        assertThat(target.getGrade()).isEqualTo(4);
        assertThat(target.getBaekjoonId()).isEqualTo("new_boj");
        assertThat(target.getStudentId()).isEqualTo("20200002");
        assertThat(target.getGithubId()).isEqualTo("new-id");
        assertThat(target.getAcademicStatus()).isEqualTo(AcademicStatus.GRADUATED);
        assertThat(target.getEmail()).isEqualTo("member@example.com");
        assertThat(target.getPassword()).isEqualTo("hash");
        assertThat(target.isVerified()).isTrue();
        assertThat(target.getRole()).isEqualTo(UserRole.USER);
    }

    @Test void rejectsDuplicateStudentIdBeforeMutation() {
        User target = user(2, UserRole.USER);
        when(users.existsByStudentId("20200002")).thenReturn(true);
        assertThatThrownBy(() -> service.apply(target, details())).isInstanceOf(CustomException.class);
        assertThat(target.getName()).isEqualTo("Member");
    }

    @Test void rejectsOrdinaryUserAdminRequest() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1, UserRole.USER)));
        assertThatThrownBy(() -> service.updateAsAdmin(1L, 2L, details(), null))
                .isInstanceOf(CustomException.class);
        verify(users, never()).findById(2L);
        verifyNoInteractions(audit);
    }

    @Test void staffCannotEditAnotherAdministrator() {
        User target = user(2, UserRole.SUPER_ADMIN);
        when(users.findById(1L)).thenReturn(Optional.of(user(1, UserRole.STAFF)));
        when(users.findById(2L)).thenReturn(Optional.of(target));
        assertThatThrownBy(() -> service.updateAsAdmin(1L, 2L, details(), null))
                .isInstanceOf(CustomException.class);
        assertThat(target.getName()).isEqualTo("Member");
        verifyNoInteractions(audit);
    }

    @Test void staffCanEditMemberAndCreatesAuditEntry() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1, UserRole.STAFF)));
        when(users.findById(2L)).thenReturn(Optional.of(user(2, UserRole.USER)));
        assertThat(service.updateAsAdmin(1L, 2L, details(), null).getName()).isEqualTo("Changed");
        verify(audit).log(any(), any(), eq(2L), any(), eq("Updated member details"), isNull());
    }

    @Test void unauthenticatedAccountReadIsDenied() {
        assertThatThrownBy(() -> service.getAccount(null)).isInstanceOf(CustomException.class);
        verifyNoInteractions(users);
    }

    @Test void legacyProfileRequestCannotChangeLoginEmail() {
        User target = user(1, UserRole.USER);
        when(users.findById(1L)).thenReturn(Optional.of(target));
        UserDirectoryService directory = new UserDirectoryService(users, service);
        UserProfileRequest request = new UserProfileRequest(null, "other@example.com", null, null,
                null, null, null, null, null, null, null, null, null, null);
        assertThatThrownBy(() -> directory.updateMyProfile(1L, request)).isInstanceOf(CustomException.class);
        assertThat(target.getEmail()).isEqualTo("member@example.com");
    }

    @Test void rejectsInvalidDetailsAtValidationBoundary() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var invalid = new UserDetailsRequest(" ", "", LocalDate.now().plusDays(2), null,
                    "", "", "", 9, "https://github.com/user", "", "javascript:alert(1)", null);
            assertThat(factory.getValidator().validate(invalid)).hasSizeGreaterThanOrEqualTo(7);
            assertThat(factory.getValidator().validate(details())).isEmpty();
        }
    }
}
