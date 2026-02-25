package com.example.coalawebbackend.domain.user.controller;

import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userFacade.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}

