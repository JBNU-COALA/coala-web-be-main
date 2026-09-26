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
        Board noticeBoard = Board.builder().name("공지").categoryKey("notice").build();

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
        Board noticeBoard = Board.builder().name("공지").categoryKey("notice").build();

        permissionService.assertCanCreatePost(admin, noticeBoard);
    }

    @Test
    void noticeAuthorizationSurvivesRenameAndDoesNotInferFromDisplayName() {
        User user = User.builder().role(UserRole.USER).build();
        Board notice = Board.builder().name("공지").categoryKey("notice").build();
        notice.updateBoard("Renamed announcements", "Description", true);
        assertThatThrownBy(() -> permissionService.assertCanCreatePost(user, notice))
                .isInstanceOf(CustomException.class);
        var post = com.example.coalawebbackend.domain.post.entity.Post.create("Title", "Body", notice, user);
        assertThatThrownBy(() -> permissionService.assertCanComment(user, post)).isInstanceOf(CustomException.class);
        permissionService.assertCanCreatePost(user, Board.builder().name("공지").categoryKey("free").build());
    }
}
