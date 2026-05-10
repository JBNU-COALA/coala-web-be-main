package com.example.coalawebbackend.api.services.controller;

import com.example.coalawebbackend.api.services.dto.InstanceApplicationRequest;
import com.example.coalawebbackend.api.services.dto.InstanceApplicationResponse;
import com.example.coalawebbackend.api.services.dto.InstanceApplicationUpdateRequest;
import com.example.coalawebbackend.api.services.dto.MemberServiceRequest;
import com.example.coalawebbackend.api.services.dto.MemberServiceResponse;
import com.example.coalawebbackend.api.services.dto.ServiceInquiryRequest;
import com.example.coalawebbackend.api.services.dto.ServiceInquiryResponse;
import com.example.coalawebbackend.api.services.service.InstanceApplicationService;
import com.example.coalawebbackend.api.services.service.MemberServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/services")
@Tag(name = "Services API", description = "유저 서비스, 인스턴스 신청, 문의사항 API")
public class ServicesController {

    private final MemberServiceCatalogService memberServiceCatalogService;
    private final InstanceApplicationService instanceApplicationService;

    @GetMapping
    @Operation(summary = "유저 서비스 목록 조회", description = "유저 서비스 카탈로그를 조회합니다.")
    public ResponseEntity<List<MemberServiceResponse>> getServices() {
        return ResponseEntity.ok(memberServiceCatalogService.getServices());
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "유저 서비스 상세 조회", description = "서비스 ID로 서비스 상세 정보를 조회합니다.")
    public ResponseEntity<MemberServiceResponse> getService(@PathVariable String serviceId) {
        return ResponseEntity.ok(memberServiceCatalogService.getService(serviceId));
    }

    @PostMapping
    @Operation(summary = "유저 서비스 등록", description = "유저 서비스를 등록합니다.")
    public ResponseEntity<MemberServiceResponse> createService(
            @Valid @RequestBody MemberServiceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memberServiceCatalogService.createService(request));
    }

    @GetMapping("/instances/applications")
    @Operation(summary = "인스턴스 신청 목록 조회", description = "인스턴스 신청 내역을 조회합니다.")
    public ResponseEntity<List<InstanceApplicationResponse>> getInstanceApplications() {
        return ResponseEntity.ok(instanceApplicationService.getApplications());
    }

    @GetMapping("/instances/applications/{applicationId}")
    @Operation(summary = "인스턴스 신청 상세 조회", description = "인스턴스 신청 ID로 상세 내역을 조회합니다.")
    public ResponseEntity<InstanceApplicationResponse> getInstanceApplication(
            @PathVariable String applicationId
    ) {
        return ResponseEntity.ok(instanceApplicationService.getApplication(applicationId));
    }

    @PostMapping("/instances/applications")
    @Operation(summary = "인스턴스 신청 생성", description = "인스턴스 대여 신청을 생성합니다.")
    public ResponseEntity<InstanceApplicationResponse> createInstanceApplication(
            @Valid @RequestBody InstanceApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instanceApplicationService.createApplication(request));
    }

    @PatchMapping("/instances/applications/{applicationId}")
    @Operation(summary = "인스턴스 신청 수정/처리", description = "신청 내용 또는 승인/반려 상태를 변경합니다.")
    public ResponseEntity<InstanceApplicationResponse> updateInstanceApplication(
            @PathVariable String applicationId,
            @Valid @RequestBody InstanceApplicationUpdateRequest request
    ) {
        return ResponseEntity.ok(instanceApplicationService.updateApplication(applicationId, request));
    }

    @GetMapping("/instances/inquiries")
    @Operation(summary = "인스턴스 문의 목록 조회", description = "인스턴스 관련 문의사항을 조회합니다.")
    public ResponseEntity<List<ServiceInquiryResponse>> getInstanceInquiries() {
        return ResponseEntity.ok(instanceApplicationService.getInquiries());
    }

    @PostMapping("/instances/inquiries")
    @Operation(summary = "인스턴스 문의 등록", description = "인스턴스 관련 문의사항을 등록합니다.")
    public ResponseEntity<ServiceInquiryResponse> createInstanceInquiry(
            @Valid @RequestBody ServiceInquiryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instanceApplicationService.createInquiry(request));
    }
}
