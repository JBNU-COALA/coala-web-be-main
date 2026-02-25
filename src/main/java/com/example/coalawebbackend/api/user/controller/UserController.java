package com.example.coalawebbackend.api.user.controller;

import com.example.coalawebbackend.api.user.facade.UserFacade;
import com.example.coalawebbackend.domain.user.entity.User;
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
public class UserController implements UserControllerSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userFacade.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}

