package com.example.coalawebbackend.api.services.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ServiceInquiryUpdateRequest(
        @NotNull @Pattern(regexp = "open|answered|closed") String status,
        @NotNull @Size(max = 5000) String reply
) {
    @AssertTrue(message = "An answered inquiry requires a reply")
    public boolean isAnswerPresent() {
        return !"answered".equals(status) || (reply != null && !reply.isBlank());
    }
}
