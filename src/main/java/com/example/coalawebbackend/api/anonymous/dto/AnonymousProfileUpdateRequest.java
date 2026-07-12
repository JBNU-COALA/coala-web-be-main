package com.example.coalawebbackend.api.anonymous.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnonymousProfileUpdateRequest {

    @NotBlank(message = "표시할 이름은 필수입니다.")
    @Size(max = 50, message = "표시할 이름은 50자 이내여야 합니다.")
    private String displayName;
}
