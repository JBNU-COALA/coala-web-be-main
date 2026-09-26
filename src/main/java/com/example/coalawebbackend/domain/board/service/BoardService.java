package com.example.coalawebbackend.domain.board.service;

import com.example.coalawebbackend.api.board.dto.BoardResponse;
import com.example.coalawebbackend.api.board.dto.CreateBoardRequest;
import com.example.coalawebbackend.api.board.dto.CreateBoardResponse;
import com.example.coalawebbackend.api.board.dto.UpdateBoardRequest;
import com.example.coalawebbackend.api.board.dto.UpdateBoardResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.entity.BoardType;
import com.example.coalawebbackend.domain.board.repository.BoardRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private static final Set<String> CATEGORY_KEYS = Set.of("notice", "free", "humor", "news", "contest", "lab", "resource");

    private final BoardRepository boardRepository;
    private final PermissionService permissionService;

    @Transactional
    public CreateBoardResponse createBoard(CreateBoardRequest request, User user) {
        permissionService.assertModerator(user);
        Board board = Board.createFromBoard(request.getBoardName(), request.getDescription(), request.getBoardType(), user);
        applyCategory(board, request.getCategoryKey());
        Board savedBoard = boardRepository.save(board);
        return CreateBoardResponse.from(savedBoard);
    }


    public List<BoardResponse> getBoards(Boolean isActive) {
        return boardRepository.findByIsActiveCondition(isActive)
                .stream()
                .map(BoardResponse::from)
                .toList();
    }

    @Transactional
    public UpdateBoardResponse updateBoard(Long boardId, UpdateBoardRequest request, User user) {
        permissionService.assertModerator(user);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (request.getCategoryKey() != null) applyCategory(board, request.getCategoryKey());
        board.updateBoard(request.getBoardName(), request.getDescription(), request.getIsActive());
        return UpdateBoardResponse.of(boardId);
    }

    @Transactional
    public void deleteBoard(Long boardId, User user) {
        permissionService.assertModerator(user);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        board.deactivate();
    }

    @Transactional(readOnly = true)
    public Board getBoardById(Long boardId) {

        return boardRepository.findById(boardId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.BOARD_NOT_FOUND)
                );
    }

    private void applyCategory(Board board, String key) {
        if (key != null && (board.getType() != BoardType.NORMAL || !CATEGORY_KEYS.contains(key))) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
        board.updateCategoryKey(board.getType() == BoardType.NORMAL ? (key == null ? "free" : key) : null);
    }

}
