package com.example.coalawebbackend.api.user.controller;

import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import com.example.coalawebbackend.api.user.dto.UserCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User API", description = "사용자 관련 API")
public interface UserControllerSpec {

    @Operation(summary = "회원 가입", description = "동아리 사용자 계정을 생성하고 JWT 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "사용자 생성 성공 (accessToken, refreshToken, user 포함)",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @RequestBody(
            required = true,
            content = @Content(schema = @Schema(implementation = UserCreateRequest.class))
    )
    ResponseEntity<TokenResponse> createUser(UserCreateRequest request);
}
