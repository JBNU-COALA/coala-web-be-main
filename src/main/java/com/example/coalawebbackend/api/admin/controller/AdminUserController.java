package com.example.coalawebbackend.api.admin.controller;

import com.example.coalawebbackend.api.admin.dto.AdminUserRoleRequest;
import com.example.coalawebbackend.api.user.dto.UserResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.moderation.entity.AdminActionType;
import com.example.coalawebbackend.domain.moderation.entity.ModerationTargetType;
import com.example.coalawebbackend.domain.moderation.service.AdminAuditService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PermissionService permissionService;
    private final AdminAuditService adminAuditService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(@AuthenticationPrincipal String adminId) {
        permissionService.assertModerator(userService.findById(adminId));
        return ResponseEntity.ok(userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(UserResponse::from)
                .toList());
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateRole(
            @AuthenticationPrincipal String adminId,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        User admin = userService.findById(adminId);
        permissionService.assertModerator(admin);
        User target = userService.findById(String.valueOf(userId));
        UserRole beforeRole = target.getRole();
        if ((request.role().canModerate() || target.getRole().canModerate())
                && !admin.getRole().atLeast(UserRole.SUPER_ADMIN)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        target.grantRole(request.role());
        adminAuditService.log(
                admin,
                ModerationTargetType.USER,
                target.getId(),
                AdminActionType.UPDATE_USER_ROLE,
                beforeRole + " -> " + target.getRole(),
                httpRequest);
        return ResponseEntity.ok(UserResponse.from(userRepository.save(target)));
    }
}
