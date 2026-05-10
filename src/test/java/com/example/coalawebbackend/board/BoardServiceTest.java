package com.example.coalawebbackend.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.willThrow;

import com.example.coalawebbackend.api.board.dto.BoardResponse;
import com.example.coalawebbackend.api.board.dto.CreateBoardRequest;
import com.example.coalawebbackend.api.board.dto.CreateBoardResponse;
import com.example.coalawebbackend.api.board.dto.UpdateBoardRequest;
import com.example.coalawebbackend.api.board.dto.UpdateBoardResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.repository.BoardRepository;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PermissionService permissionService;

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard_success() {
        // given
        User user = mock(User.class);
        CreateBoardRequest request = mock(CreateBoardRequest.class);
        Board board = mock(Board.class);

        given(boardRepository.save(any(Board.class))).willReturn(board);

        // when
        CreateBoardResponse response = boardService.createBoard(request, user);

        // then
        assertThat(response).isNotNull();
        then(permissionService).should(times(1)).assertModerator(user);
        then(boardRepository).should(times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getBoards_success() {
        // given
        Board board = mock(Board.class);
        given(boardRepository.findByIsActiveCondition(true)).willReturn(List.of(board));

        // when
        List<BoardResponse> result = boardService.getBoards(true);

        // then
        assertThat(result).hasSize(1);
        then(boardRepository).should(times(1)).findByIsActiveCondition(true);
    }
    @Test
    @DisplayName("게시글 수정 성공")
    void updateBoard_success() {
        // given
        Long boardId = 1L;
        User user = mock(User.class);
        Board board = mock(Board.class);
        UpdateBoardRequest request = mock(UpdateBoardRequest.class);

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        UpdateBoardResponse response = boardService.updateBoard(boardId, request, user);

        // then
        assertThat(response).isNotNull();
        then(permissionService).should(times(1)).assertModerator(user);
        then(board).should(times(1)).updateBoard(request.getBoardName(),request.getDescription(),
                request.getIsActive());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void updateBoard_fail_boardNotFound() {
        // given
        Long boardId = 1L;
        User user = mock(User.class);
        UpdateBoardRequest request = mock(UpdateBoardRequest.class);

        given(boardRepository.findById(boardId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardService.updateBoard(boardId, request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.BOARD_NOT_FOUND));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자 불일치")
    void updateBoard_fail_accessDenied() {
        // given
        Long boardId = 1L;
        User other = mock(User.class);
        UpdateBoardRequest request = mock(UpdateBoardRequest.class);

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(permissionService).assertModerator(other);

        // when & then
        assertThatThrownBy(() -> boardService.updateBoard(boardId, request, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deleteBoard_success() {
        // given
        Long boardId = 1L;
        User user = mock(User.class);
        Board board = mock(Board.class);

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        boardService.deleteBoard(boardId, user);

        // then
        then(permissionService).should(times(1)).assertModerator(user);
        then(board).should(times(1)).deactivate();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자 불일치")
    void deleteBoard_fail_accessDenied() {
        // given
        Long boardId = 1L;
        User other = mock(User.class);

        willThrow(new CustomException(ErrorCode.ACCESS_DENIED))
                .given(permissionService).assertModerator(other);

        // when & then
        assertThatThrownBy(() -> boardService.deleteBoard(boardId, other))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
