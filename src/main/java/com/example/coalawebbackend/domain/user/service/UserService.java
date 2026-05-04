package com.example.coalawebbackend.domain.user.service;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        validateUniqueUser(user);
        return userRepository.save(user);
    }

    public User findById(String userId) {
        try {
            return userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_USER_ID);
        }
    }

    private void validateUniqueUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByStudentId(user.getStudentId())) {
            throw new CustomException(ErrorCode.DUPLICATE_STUDENT_ID);
        }
        if (userRepository.existsByGithubId(user.getGithubId())) {
            throw new CustomException(ErrorCode.DUPLICATE_GITHUB_ID);
        }
        String linkedinUrl = user.getLinkedinUrl();
        if (linkedinUrl != null
                && !linkedinUrl.isBlank()
                && userRepository.existsByLinkedinUrl(linkedinUrl)) {
            throw new CustomException(ErrorCode.DUPLICATE_LINKEDIN_URL);
        }
    }
}
