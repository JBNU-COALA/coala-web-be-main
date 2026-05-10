package com.example.coalawebbackend.domain.moderation.service;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.entity.UserSanction;
import com.example.coalawebbackend.domain.moderation.entity.UserSanctionType;
import com.example.coalawebbackend.domain.moderation.repository.UserSanctionRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SanctionPolicyService {

    private static final EnumSet<UserSanctionType> POST_BLOCKING = EnumSet.of(
            UserSanctionType.POST_RESTRICTED,
            UserSanctionType.TEMP_SUSPENDED,
            UserSanctionType.ACCOUNT_SUSPENDED,
            UserSanctionType.PERMANENT_BANNED);

    private static final EnumSet<UserSanctionType> COMMENT_BLOCKING = EnumSet.of(
            UserSanctionType.COMMENT_RESTRICTED,
            UserSanctionType.TEMP_SUSPENDED,
            UserSanctionType.ACCOUNT_SUSPENDED,
            UserSanctionType.PERMANENT_BANNED);

    private final UserSanctionRepository userSanctionRepository;

    public void assertCanWritePost(User user) {
        assertNoActiveSanction(user, POST_BLOCKING);
    }

    public void assertCanWriteComment(User user) {
        assertNoActiveSanction(user, COMMENT_BLOCKING);
    }

    private void assertNoActiveSanction(User user, EnumSet<UserSanctionType> blockingTypes) {
        List<UserSanction> sanctions = userSanctionRepository.findActive(user, LocalDateTime.now());
        boolean blocked = sanctions.stream()
                .map(UserSanction::getType)
                .anyMatch(blockingTypes::contains);
        if (blocked) {
            throw new CustomException(ErrorCode.USER_SANCTIONED);
        }
    }
}
