package com.example.coalawebbackend.domain.moderation.service;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ContentSafetyService {

    private static final Pattern DANGEROUS_HTML = Pattern.compile(
            "(?i)(<\\s*(script|iframe|object|embed|style)\\b|\\son[a-z]+\\s*=|javascript\\s*:)");

    public void validateMarkdown(String value) {
        if (value != null && DANGEROUS_HTML.matcher(value).find()) {
            throw new CustomException(ErrorCode.UNSAFE_CONTENT);
        }
    }
}
