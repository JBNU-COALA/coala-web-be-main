package com.example.coalawebbackend.api.study;

import com.example.coalawebbackend.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {
    private final StudyService service;
    private final UserService users;

    @GetMapping("/groups")
    public List<StudyDtos.Group> groups(@AuthenticationPrincipal String userId) {
        return service.listGroups(users.findById(userId));
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDtos.Group createGroup(@Valid @RequestBody StudyDtos.GroupRequest request, @AuthenticationPrincipal String userId) {
        return service.createGroup(request, users.findById(userId));
    }

    @GetMapping("/records")
    public List<StudyDtos.Record> records(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long groupId, @RequestParam(required = false) Long memberId,
            @AuthenticationPrincipal String userId) {
        return service.listRecords(from, to, groupId, memberId, users.findById(userId));
    }

    @GetMapping("/records/{id}")
    public StudyDtos.Record record(@PathVariable String id, @AuthenticationPrincipal String userId) {
        return service.getRecord(id, users.findById(userId));
    }

    @PostMapping("/records")
    @ResponseStatus(HttpStatus.CREATED)
    public StudyDtos.Record create(@Valid @RequestBody StudyDtos.RecordRequest request, @AuthenticationPrincipal String userId) {
        return service.createRecord(request, users.findById(userId));
    }

    @PatchMapping("/records/{id}")
    public StudyDtos.Record update(@PathVariable String id, @Valid @RequestBody StudyDtos.RecordRequest request, @AuthenticationPrincipal String userId) {
        return service.updateRecord(id, request, users.findById(userId));
    }
}
