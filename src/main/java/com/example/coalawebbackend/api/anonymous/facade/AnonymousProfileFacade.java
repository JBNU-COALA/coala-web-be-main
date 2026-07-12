package com.example.coalawebbackend.api.anonymous.facade;

import com.example.coalawebbackend.api.anonymous.dto.AnonymousProfileResponse;
import com.example.coalawebbackend.api.anonymous.dto.AnonymousProfileUpdateRequest;
import com.example.coalawebbackend.domain.anonymous.entity.AnonymousProfile;
import com.example.coalawebbackend.domain.anonymous.service.AnonymousProfileService;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AnonymousProfileFacade {

    private final UserService userService;
    private final BoardService boardService;
    private final AnonymousProfileService anonymousProfileService;

    @Transactional
    public AnonymousProfileResponse getMyProfile(Long boardId, String userId) {
        User user = userService.findById(userId);
        Board board = boardService.getBoardById(boardId);
        AnonymousProfile profile = anonymousProfileService.getOrCreateProfile(board, user);
        return AnonymousProfileResponse.from(profile);
    }

    @Transactional
    public AnonymousProfileResponse updateMyProfile(Long boardId, String userId, AnonymousProfileUpdateRequest request) {
        User user = userService.findById(userId);
        Board board = boardService.getBoardById(boardId);
        AnonymousProfile profile = anonymousProfileService.updateDisplayName(board, user, request.getDisplayName());
        return AnonymousProfileResponse.from(profile);
    }
}
