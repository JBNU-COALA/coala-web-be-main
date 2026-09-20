package com.example.coalawebbackend.api.study;

import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.attachment.service.AttachmentService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.moderation.service.SanctionPolicyService;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.repository.RecruitApplicationRepository;
import com.example.coalawebbackend.domain.recruit.repository.RecruitPostRepository;
import com.example.coalawebbackend.domain.study.*;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {
    private final StudyGroupRepository groups;
    private final StudyRecordRepository records;
    private final RecruitPostRepository recruits;
    private final RecruitApplicationRepository applications;
    private final PermissionService permissions;
    private final SanctionPolicyService sanctions;
    private final AttachmentService attachments;
    private final UserRepository users;

    private void assertVerified(User actor) {
        if (actor == null) throw new CustomException(ErrorCode.ACCESS_DENIED);
        if (!actor.isVerified()) throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    private void assertManage(User actor, StudyGroup group) {
        assertVerified(actor);
        permissions.assertCanManageRecruit(actor, group.getRecruit());
        sanctions.assertCanWritePost(actor);
    }

    public List<StudyDtos.MemberOption> searchMembers(String query, User actor) {
        assertVerified(actor);
        String term = query == null ? "" : query.trim();
        if (term.length() > 80) throw new CustomException(ErrorCode.VALIDATION_FAILED);
        return users.searchActivityMembers(term, PageRequest.of(0, 20, Sort.by("name").ascending().and(Sort.by("id"))))
                .stream().map(user -> new StudyDtos.MemberOption(user.getId().toString(), user.getName(), user.getGithubId(), user.getDepartment()))
                .toList();
    }

    public List<StudyDtos.Group> listGroups(User actor) {
        assertVerified(actor);
        List<StudyGroup> result = groups.findAllByOrderByIdDesc();
        if (result.isEmpty()) return List.of();
        Map<String, Map<Long, User>> rosters = new HashMap<>();
        for (StudyGroup group : result) {
            Map<Long, User> roster = new LinkedHashMap<>();
            User owner = group.getRecruit().getAuthor();
            if (owner != null) roster.put(owner.getId(), owner);
            rosters.put(group.getRecruit().getId(), roster);
        }
        applications.findByRecruitPost_IdInAndStatus(new ArrayList<>(rosters.keySet()), "accepted")
                .forEach(application -> {
                    User user = application.getUser();
                    if (user != null) rosters.get(application.getRecruitPost().getId()).put(user.getId(), user);
                });
        return result.stream().map(group -> toGroup(group, actor,
                new ArrayList<>(rosters.get(group.getRecruit().getId()).values()))).toList();
    }

    @Transactional
    public StudyDtos.Group createGroup(StudyDtos.GroupRequest request, User actor) {
        assertVerified(actor);
        RecruitPost recruit = recruits.findForUpdate(request.recruitId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        permissions.assertCanManageRecruit(actor, recruit);
        sanctions.assertCanWritePost(actor);
        StudyGroup group = groups.findByRecruit_Id(recruit.getId())
                .orElseGet(() -> groups.save(new StudyGroup(recruit, request.name().trim())));
        return toGroup(group, actor);
    }

    public List<StudyDtos.Record> listRecords(LocalDate from, LocalDate to, Long groupId, Long userId, User actor) {
        assertVerified(actor);
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 93) {
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        }
        return records.findByDateBetweenOrderByDateDescUpdatedAtDesc(from, to).stream()
                .filter(record -> groupId == null || (record.getGroup() != null && record.getGroup().getId().equals(groupId)))
                .filter(record -> userId == null || record.getAuthor().getId().equals(userId) || record.getAttendance().stream().anyMatch(entry -> entry.getUser().getId().equals(userId)))
                .map(record -> toRecord(record, actor)).toList();
    }

    public StudyDtos.Record getRecord(String id, User actor) {
        assertVerified(actor);
        return toRecord(findRecord(id), actor);
    }

    @Transactional
    public StudyDtos.Record createRecord(StudyDtos.RecordRequest request, User actor) {
        assertVerified(actor);
        sanctions.assertCanWritePost(actor);
        StudyGroup group = requestedGroup(request.groupId(), actor);
        StudyRecord record = new StudyRecord(group, actor);
        update(record, request, actor);
        records.saveAndFlush(record);
        attachments.syncStudyPhotos(actor, record.getId(), request.attachmentIds());
        return toRecord(record, actor);
    }

    @Transactional
    public StudyDtos.Record updateRecord(String id, StudyDtos.RecordRequest request, User actor) {
        StudyRecord record = findRecord(id);
        assertManageRecord(actor, record);
        sanctions.assertCanWritePost(actor);
        Long currentGroupId = record.getGroup() == null ? null : record.getGroup().getId();
        if (currentGroupId != null && !currentGroupId.equals(request.groupId())) throw new CustomException(ErrorCode.VALIDATION_FAILED);
        if (request.version() == null || !request.version().equals(record.getVersion())) throw new CustomException(ErrorCode.POST_NOT_EDITABLE);
        if (currentGroupId == null && request.groupId() != null) {
            StudyGroup group = requestedGroup(request.groupId(), actor);
            record.attachGroup(group);
        }
        update(record, request, actor);
        records.saveAndFlush(record);
        attachments.syncStudyPhotos(actor, record.getId(), request.attachmentIds());
        return toRecord(record, actor);
    }

    private StudyGroup requestedGroup(Long id, User actor) {
        if (id == null) return null;
        StudyGroup group = groups.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        assertManage(actor, group);
        return group;
    }

    private boolean canManageRecord(User actor, StudyRecord record) {
        return record.getGroup() == null
                ? actor != null && (actor.getId().equals(record.getAuthor().getId()) || permissions.canModerate(actor))
                : permissions.canManageRecruit(actor, record.getGroup().getRecruit());
    }

    private void assertManageRecord(User actor, StudyRecord record) {
        assertVerified(actor);
        if (!canManageRecord(actor, record)) throw new CustomException(ErrorCode.ACCESS_DENIED);
    }

    @Transactional
    public void deleteRecord(String id, Long version, User actor) {
        StudyRecord record = findRecord(id);
        assertManageRecord(actor, record);
        if (version == null || !version.equals(record.getVersion())) throw new CustomException(ErrorCode.POST_NOT_EDITABLE);
        attachments.deleteStudyPhotos(actor, record.getId());
        records.delete(record);
        records.flush();
    }

    private void update(StudyRecord record, StudyDtos.RecordRequest request, User actor) {
        List<Long> supplied = request.attendance().stream().map(StudyDtos.AttendanceRequest::userId).toList();
        Set<Long> unique = new HashSet<>(supplied);
        if (supplied.size() > 200 || unique.size() != supplied.size() || unique.contains(null))
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        Map<Long, User> byId = new HashMap<>();
        users.findAllById(unique).forEach(user -> byId.put(user.getId(), user));
        if (byId.size() != unique.size()) throw new CustomException(ErrorCode.VALIDATION_FAILED);
        // Preserve historical participants; newly added members must be verified.
        Set<Long> existing = new HashSet<>(record.getAttendance().stream().map(entry -> entry.getUser().getId()).toList());
        if (byId.values().stream().anyMatch(user -> !user.isVerified() && !existing.contains(user.getId())))
            throw new CustomException(ErrorCode.VALIDATION_FAILED);
        record.update(request.title(), request.date(), request.content(), request.attendance().stream()
                .map(entry -> new StudyAttendance(byId.get(entry.userId()), entry.status())).toList(), actor);
    }

    private StudyRecord findRecord(String id) {
        return records.findById(id).orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private List<User> members(RecruitPost recruit) {
        Map<Long, User> members = new LinkedHashMap<>();
        if (recruit.getAuthor() != null) members.put(recruit.getAuthor().getId(), recruit.getAuthor());
        applications.findByRecruitPost_IdOrderBySubmittedAtDesc(recruit.getId()).stream()
                .filter(application -> "accepted".equals(application.getStatus()) && application.getUser() != null)
                .forEach(application -> members.put(application.getUser().getId(), application.getUser()));
        return new ArrayList<>(members.values());
    }

    private StudyDtos.Group toGroup(StudyGroup group, User actor) {
        return toGroup(group, actor, members(group.getRecruit()));
    }

    private StudyDtos.Group toGroup(StudyGroup group, User actor, List<User> roster) {
        return new StudyDtos.Group(group.getId().toString(), group.getRecruit().getId(), group.getName(),
                roster.stream().map(user -> new StudyDtos.Member(user.getId().toString(), user.getName())).toList(),
                permissions.canManageRecruit(actor, group.getRecruit()));
    }

    private StudyDtos.Record toRecord(StudyRecord record, User actor) {
        return new StudyDtos.Record(record.getId(), record.getGroup() == null ? null : record.getGroup().getId().toString(), record.getTitle(), record.getDate(), record.getContent(),
                record.getAttendance().stream().map(entry -> new StudyDtos.Attendance(entry.getUser().getId().toString(), entry.getUser().getName(), entry.getStatus())).toList(),
                record.getUpdatedAt().toString(), record.getVersion(), canManageRecord(actor, record), record.getAuthor().getId().toString(),
                attachments.studyPhotos(record.getId()).stream().map(photo -> new StudyDtos.Photo(photo.getId(), photo.getOriginalName())).toList());
    }
}
