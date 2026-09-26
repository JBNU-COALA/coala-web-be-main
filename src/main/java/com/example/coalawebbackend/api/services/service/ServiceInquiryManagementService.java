package com.example.coalawebbackend.api.services.service;

import com.example.coalawebbackend.api.services.dto.ServiceInquiryResponse;
import com.example.coalawebbackend.api.services.dto.ServiceInquiryUpdateRequest;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.instance.repository.ServiceInquiryRepository;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceInquiryManagementService {
    private final ServiceInquiryRepository inquiries;
    private final PermissionService permissions;

    @Transactional
    public ServiceInquiryResponse update(User actor, String id, String prefix, ServiceInquiryUpdateRequest request) {
        permissions.assertModerator(actor);
        var inquiry = inquiries.findById(id).filter(row -> row.getId().startsWith(prefix))
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        inquiry.respond(request.status(), request.reply());
        return ServiceInquiryResponse.from(inquiry);
    }
}
