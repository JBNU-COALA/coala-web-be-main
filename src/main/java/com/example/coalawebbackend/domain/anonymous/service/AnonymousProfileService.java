package com.example.coalawebbackend.domain.anonymous.service;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.anonymous.entity.AnonymousProfile;
import com.example.coalawebbackend.domain.anonymous.repository.AnonymousProfileRepository;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.entity.BoardType;
import com.example.coalawebbackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 익명 게시판에서 사용자별 익명 표시 프로필(닉네임/한줄소개 형태의 표시명)을 관리한다.
 * 표시명은 게시판 내에서 본인만 언제든 수정할 수 있으며, 수정 즉시 해당 게시판의 과거 글/댓글에도 소급 적용된다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnonymousProfileService {

    private final AnonymousProfileRepository anonymousProfileRepository;

    @Transactional
    public AnonymousProfile getOrCreateProfile(Board board, User user) {
        assertAnonymousBoard(board);
        return anonymousProfileRepository.findByBoard_BoardIdAndUser_Id(board.getBoardId(), user.getId())
                .orElseGet(() -> anonymousProfileRepository.save(AnonymousProfile.createDefault(board, user)));
    }

    public String getDisplayName(Board board, User user) {
        return anonymousProfileRepository.findByBoard_BoardIdAndUser_Id(board.getBoardId(), user.getId())
                .map(AnonymousProfile::getDisplayName)
                .orElse(AnonymousProfile.DEFAULT_DISPLAY_NAME);
    }

    @Transactional
    public AnonymousProfile updateDisplayName(Board board, User user, String displayName) {
        assertAnonymousBoard(board);
        String normalized = displayName == null ? "" : displayName.trim();
        if (normalized.isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
        AnonymousProfile profile = getOrCreateProfile(board, user);
        profile.updateDisplayName(normalized);
        return profile;
    }

    private void assertAnonymousBoard(Board board) {
        if (board.getType() != BoardType.ANONYMOUS) {
            throw new CustomException(ErrorCode.BOARD_NOT_ANONYMOUS);
        }
    }
}
