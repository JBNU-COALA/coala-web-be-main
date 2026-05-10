package com.example.coalawebbackend.domain.moderation.entity;

public enum AdminActionType {
    HIDE_POST,
    RESTORE_POST,
    DELETE_POST,
    LOCK_POST,
    UNLOCK_POST,
    HIDE_COMMENT,
    RESTORE_COMMENT,
    DELETE_COMMENT,
    SANCTION_USER,
    HANDLE_REPORT,
    UPDATE_USER_ROLE
}
