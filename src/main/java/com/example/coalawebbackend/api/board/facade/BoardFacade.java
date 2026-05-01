package com.example.coalawebbackend.api.board.facade;

import com.example.coalawebbackend.api.board.dto.BoardResponse;
import com.example.coalawebbackend.api.board.dto.CreateBoardRequest;
import com.example.coalawebbackend.api.board.dto.CreateBoardResponse;
import com.example.coalawebbackend.api.board.dto.UpdateBoardRequest;
import com.example.coalawebbackend.api.board.dto.UpdateBoardResponse;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardFacade {

    private final UserService userService;
    private final BoardService boardService;

    @Transactional
    public CreateBoardResponse createBoard(CreateBoardRequest request, String userId) {
        User user = userService.findById(userId);
        return boardService.createBoard(request, user);
    }

    public List<BoardResponse> getBoards(Boolean isActive) {
        return boardService.getBoards(isActive);
    }

    public Board getBoard(Long boardId) {
        return boardService.getBoardById(boardId);
    }

    @Transactional
    public UpdateBoardResponse updateBoard(Long boardId, UpdateBoardRequest request, String userId) {
        User user = userService.findById(userId);
        return boardService.updateBoard(boardId, request, user);
    }

    @Transactional
    public void deleteBoard(Long boardId, String userId) {
        User user = userService.findById(userId);
        boardService.deleteBoard(boardId, user);
    }
}
