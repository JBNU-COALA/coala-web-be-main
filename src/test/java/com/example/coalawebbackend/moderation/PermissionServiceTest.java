package com.example.coalawebbackend.moderation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.mock;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.moderation.service.SanctionPolicyService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermissionServiceTest {

    private final SanctionPolicyService sanctionPolicyService = mock(SanctionPolicyService.class);
    private final PermissionService permissionService = new PermissionService(sanctionPolicyService);

    @Test
    @DisplayName("공지 게시판은 일반 회원이 글을 작성할 수 없다")
    void noticeBoardCreateDeniedForRegularUser() {
        User user = User.builder().role(UserRole.USER).build();
        Board noticeBoard = Board.builder().name("공지").build();

        assertThatThrownBy(() -> permissionService.assertCanCreatePost(user, noticeBoard))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> {
                    CustomException exception = (CustomException) error;
                    org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED);
                });
    }

    @Test
    @DisplayName("공지 게시판은 운영자 이상만 글을 작성할 수 있다")
    void noticeBoardCreateAllowedForModerator() {
        User admin = User.builder().role(UserRole.STAFF).build();
        Board noticeBoard = Board.builder().name("공지").build();

        permissionService.assertCanCreatePost(admin, noticeBoard);
    }
}
