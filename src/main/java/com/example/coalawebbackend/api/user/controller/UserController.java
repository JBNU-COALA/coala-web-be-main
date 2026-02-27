package com.example.coalawebbackend.api.user.controller;

import com.example.coalawebbackend.api.auth.dto.TokenResponse;
import com.example.coalawebbackend.api.user.dto.UserCreateRequest;
import com.example.coalawebbackend.api.user.facade.UserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/signup")
@RequiredArgsConstructor
public class UserController implements UserControllerSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ResponseEntity<TokenResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        TokenResponse response = userFacade.createUser(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
