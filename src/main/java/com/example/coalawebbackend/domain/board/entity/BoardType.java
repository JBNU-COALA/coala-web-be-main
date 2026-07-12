package com.example.coalawebbackend.domain.board.entity;

/**
 * 게시판 유형.
 * - NORMAL: 일반 게시판
 * - RECRUIT: 모집 게시판
 * - ANONYMOUS: 익명 게시판 (작성자 실명 대신 게시판별 익명 프로필로 노출)
 */
public enum BoardType {
    NORMAL,
    RECRUIT,
    ANONYMOUS
}
