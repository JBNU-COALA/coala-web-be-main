package com.example.coalawebbackend.study;

import static org.assertj.core.api.Assertions.*;

import com.example.coalawebbackend.api.recruit.service.RecruitService;
import com.example.coalawebbackend.api.study.*;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.recruit.entity.*;
import com.example.coalawebbackend.domain.recruit.repository.*;
import com.example.coalawebbackend.domain.user.entity.*;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:study-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false", "jwt.secret=study-integration-test-secret-value",
    "spring.jpa.show-sql=false"
})
class StudyIntegrationTest {
    @Autowired StudyService service;
    @Autowired RecruitService recruitment;
    @Autowired UserRepository users;
    @Autowired RecruitPostRepository recruits;
    @Autowired RecruitApplicationRepository applications;
    @Autowired EntityManager em;
    @Autowired Validator validator;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired com.example.coalawebbackend.common.jwt.JwtTokenProvider tokens;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.example.coalawebbackend.common.jwt.LogoutTokenStore logoutTokens;
    @Autowired com.example.coalawebbackend.domain.attachment.service.AttachmentService attachments;
    @Autowired com.example.coalawebbackend.domain.attachment.repository.AttachmentRepository attachmentRows;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.example.coalawebbackend.infra.storage.FileStorage storage;
    User owner;
    User member;
    User outsider;
    RecruitPost recruit;
    RecruitApplication application;

    @BeforeEach
    void setup() {
        owner = user("Owner", true);
        member = user("Member", true);
        outsider = user("Other", true);
        recruit = RecruitPost.builder().id(UUID.randomUUID().toString()).author(owner)
            .title("Study").shortDesc("Weekly study").category("study").status("open")
            .host("Owner").hostInitials("O").hostTone("mint").hostRole("host")
            .maxMembers(1).meetingType("online").expectedDuration("8 weeks").build();
        recruit.addRole(RecruitRole.builder().label("Member").max(1).current(0).sortOrder(0).build());
        recruit = recruits.saveAndFlush(recruit);
        application = applications.saveAndFlush(RecruitApplication.builder().recruitPost(recruit)
            .user(member).role("Member").body("Join").status("submitted").submittedAt(LocalDateTime.now()).build());
    }

    User user(String name, boolean verified) {
        return user(name, verified, UserRole.USER);
    }

    User user(String name, boolean verified, UserRole role) {
        String key = UUID.randomUUID().toString();
        return users.saveAndFlush(User.builder().email(key + "@jbnu.ac.kr").password("test-only")
            .name(name).githubId(key).studentId(key.substring(0, 18)).department("CS")
            .academicStatus(AcademicStatus.ENROLLED).verified(verified).role(role).build());
    }

    StudyDtos.Group group() {
        return service.createGroup(new StudyDtos.GroupRequest(recruit.getId(), "Group 1"), owner);
    }

    StudyDtos.RecordRequest request(StudyDtos.Group group, Long version, Long... ids) {
        return new StudyDtos.RecordRequest(Long.valueOf(group.id()), "Week 1", LocalDate.now(), "# Notes\nLearned React",
            java.util.Arrays.stream(ids).map(id -> new StudyDtos.AttendanceRequest(id, "present")).toList(), version);
    }

    com.example.coalawebbackend.domain.attachment.entity.Attachment photo(User uploader) {
        return attachmentRows.saveAndFlush(com.example.coalawebbackend.domain.attachment.entity.Attachment.builder()
            .uploader(uploader).targetType(com.example.coalawebbackend.domain.attachment.entity.AttachmentTargetType.STUDY_RECORD)
            .fileCategory(com.example.coalawebbackend.domain.attachment.entity.FileCategory.IMAGE)
            .originalName("proof.png").storedName(UUID.randomUUID() + ".png").storagePath("test/proof.png")
            .contentType("image/png").fileSize(10).build());
    }

    @Test void studyPhotosArePrivateAndSurviveLegacyEditsThenDeleteWithRecord() throws Exception {
        var photo = photo(owner);
        org.mockito.Mockito.when(storage.exists(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(storage.load(org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(new org.springframework.core.io.ByteArrayResource(new byte[10]));
        assertThatThrownBy(() -> attachments.getDownload(photo.getId())).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> attachments.getDownload(photo.getId(), outsider)).isInstanceOf(CustomException.class);
        assertThat(attachments.getDownload(photo.getId(), owner).fileSize()).isEqualTo(10);
        var created = service.createRecord(new StudyDtos.RecordRequest(null, "Photos", LocalDate.now(), "Evidence", List.of(), null, List.of(photo.getId())), owner);
        assertThat(created.photos()).extracting(StudyDtos.Photo::attachmentId).containsExactly(photo.getId());
        assertThat(photo.isActive()).isTrue();
        assertThat(attachments.getDownload(photo.getId(), member).fileSize()).isEqualTo(10);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/attachments/" + photo.getId() + "/download"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        String token = tokens.createToken(member.getId().toString(), java.util.Map.of("role", "ROLE_USER"), 60000);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/attachments/" + photo.getId() + "/download")
                .header("Authorization", "Bearer " + token))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Cache-Control", "private, no-store"));
        var updated = service.updateRecord(created.id(), new StudyDtos.RecordRequest(null, "Edited", LocalDate.now(), "Evidence", List.of(), created.version()), owner);
        assertThat(updated.photos()).hasSize(1);
        service.deleteRecord(updated.id(), updated.version(), owner);
        assertThatThrownBy(() -> attachments.getDownload(photo.getId(), owner)).isInstanceOf(CustomException.class);
    }

    @Test void studyPhotoRemovalAndOwnershipAreEnforced() {
        var own = photo(owner);
        var other = photo(outsider);
        assertThatThrownBy(() -> service.createRecord(new StudyDtos.RecordRequest(null, "Photos", LocalDate.now(), "Evidence", List.of(), null, List.of(other.getId())), owner))
            .isInstanceOf(CustomException.class);
        var created = service.createRecord(new StudyDtos.RecordRequest(null, "Photos", LocalDate.now(), "Evidence", List.of(), null, List.of(own.getId())), owner);
        assertThatThrownBy(() -> service.createRecord(new StudyDtos.RecordRequest(null, "Reuse", LocalDate.now(), "Evidence", List.of(), null, List.of(own.getId())), owner))
            .isInstanceOf(CustomException.class);
        var removed = service.updateRecord(created.id(), new StudyDtos.RecordRequest(null, "Edited", LocalDate.now(), "Evidence", List.of(), created.version(), List.of()), owner);
        assertThat(removed.photos()).isEmpty();
        assertThat(own.getStatus()).isEqualTo(com.example.coalawebbackend.domain.attachment.entity.AttachmentStatus.DELETED);
        var tooMany = new StudyDtos.RecordRequest(null, "Photos", LocalDate.now(), "Evidence", List.of(), null, List.of(1L, 2L, 3L, 4L, 5L, 6L));
        assertThat(validator.validate(tooMany)).isNotEmpty();
    }

    @Test void photoUploadReservesPrivateAttachmentAndRejectsUnverifiedUsers() throws Exception {
        org.mockito.Mockito.when(storage.store(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
            .thenReturn(new com.example.coalawebbackend.infra.storage.StoredFile("proof.png", "proof.png", "test/proof.png", "image/png", 10, "png", "test"));
        String token = tokens.createToken(owner.getId().toString(), java.util.Map.of("role", "ROLE_USER"), 60000);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/study/photos")
                .file(new org.springframework.mock.web.MockMultipartFile("file", "proof.png", "image/png", new byte[10]))
                .header("Authorization", "Bearer " + token))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("TEMP"));
        assertThat(attachmentRows.findAll()).allMatch(photo ->
            photo.getTargetType() == com.example.coalawebbackend.domain.attachment.entity.AttachmentTargetType.STUDY_RECORD);
        assertThatThrownBy(() -> attachments.uploadStudyPhoto(user("Unverified", false),
            new org.springframework.mock.web.MockMultipartFile("file", new byte[10]))).isInstanceOf(CustomException.class);
    }

    @Test void privatePhotoCannotBeReattachedToPublicContent() {
        var own = photo(owner);
        assertThatThrownBy(() -> attachments.syncArchiveAttachment(owner, 1L, own.getId())).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> attachments.syncInfoArticleAttachments(owner, 1L, List.of(own.getId()), null)).isInstanceOf(CustomException.class);
    }

    @Test void standaloneRecordCrudAndOwnerFilter() {
        StudyDtos.RecordRequest request = new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Independent notes", List.of(), null);
        assertThat(validator.validate(request)).isEmpty();
        StudyDtos.Record created = service.createRecord(request, member);
        assertThat(created.groupId()).isNull();
        assertThat(created.authorId()).isEqualTo(member.getId().toString());
        assertThat(created.canManage()).isTrue();
        assertThat(service.getRecord(created.id(), outsider).canManage()).isFalse();
        assertThat(service.listRecords(LocalDate.now(), LocalDate.now(), null, member.getId(), member)).hasSize(1);
        assertThat(service.listRecords(LocalDate.now(), LocalDate.now(), 999L, null, member)).isEmpty();
        StudyDtos.RecordRequest changed = new StudyDtos.RecordRequest(null, "Updated", LocalDate.now(), "Updated notes", List.of(), created.version());
        assertThatThrownBy(() -> service.updateRecord(created.id(), changed, outsider)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.deleteRecord(created.id(), created.version(), outsider)).isInstanceOf(CustomException.class);
        StudyDtos.Record updated = service.updateRecord(created.id(), changed, member);
        assertThat(updated.title()).isEqualTo("Updated");
        assertThatThrownBy(() -> service.deleteRecord(created.id(), created.version(), member)).isInstanceOf(CustomException.class);
        service.deleteRecord(created.id(), updated.version(), member);
        assertThatThrownBy(() -> service.getRecord(created.id(), member)).isInstanceOf(CustomException.class);
    }

    @Test void standaloneCanBeLinkedLaterButOnlyToAnAuthorizedGroup() {
        StudyDtos.Group group = group();
        StudyDtos.Record created = service.createRecord(new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes", List.of(), null), outsider);
        assertThatThrownBy(() -> service.updateRecord(created.id(), request(group, created.version(), owner.getId()), outsider))
                .isInstanceOf(CustomException.class);
        StudyDtos.Record own = service.createRecord(new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes", List.of(), null), owner);
        StudyDtos.Record linked = service.updateRecord(own.id(), request(group, own.version(), owner.getId()), owner);
        assertThat(linked.groupId()).isEqualTo(group.id());
        assertThat(linked.attendance()).hasSize(1);
        assertThatThrownBy(() -> service.updateRecord(linked.id(),
                new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes", List.of(), linked.version()), owner))
                .isInstanceOf(CustomException.class);
    }

    @Test void standaloneRejectsForgedAttendanceAndUnverifiedAuthor() {
        StudyDtos.RecordRequest forged = new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes",
                List.of(new StudyDtos.AttendanceRequest(outsider.getId(), "present")), null);
        assertThatThrownBy(() -> service.createRecord(forged, owner)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.createRecord(new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes", List.of(), null),
                user("Unverified", false))).isInstanceOf(CustomException.class);
        User admin = user("Admin", true, UserRole.SUPER_ADMIN);
        StudyDtos.Record record = service.createRecord(new StudyDtos.RecordRequest(null, "Notes", LocalDate.now(), "Notes", List.of(), null), owner);
        assertThat(service.getRecord(record.id(), admin).canManage()).isTrue();
        service.deleteRecord(record.id(), record.version(), admin);
    }

    @Test void recruitCrudAllowsOptionalFieldsAndLongTitlesWithoutLongIds() {
        var request = new com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest(
                "Long title ".repeat(12), "Summary", "study",
                List.of(new com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest.RecruitRoleRequest("Member", 2)),
                List.of(), "Online", "One week", "open", List.of(), List.of("Content, with commas"), List.of());
        assertThat(validator.validate(request)).isEmpty();
        var created = recruitment.createRecruit(request, owner.getId().toString());
        assertThat(created.id()).hasSize(36);
        em.flush(); em.clear();
        assertThat(recruitment.getRecruit(created.id()).detailContent()).containsExactly("Content, with commas");
        assertThat(recruitment.updateRecruit(owner, created.id(), request).title()).isEqualTo(request.title().trim());
        em.flush(); em.clear();
        assertThatThrownBy(() -> recruitment.updateRecruit(outsider, created.id(), request)).isInstanceOf(CustomException.class);
        recruitment.deleteRecruit(owner, created.id());
        em.flush();
        assertThatThrownBy(() -> recruitment.getRecruit(created.id())).isInstanceOf(CustomException.class);
    }

    @Test void validatesRecruitRoleBoundsAndCategory() {
        var request = new com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest(
                "Title", "Summary", "invalid",
                List.of(new com.example.coalawebbackend.api.recruit.dto.RecruitPostRequest.RecruitRoleRequest("", -1)),
                List.of(), "Online", "One week", "forged", List.of(), List.of("Content"), List.of());
        assertThat(validator.validate(request)).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test void approvalControlsRosterAndPersistsRecord() {
        StudyDtos.Group group = group();
        assertThat(group.members()).hasSize(1);
        recruitment.decideApplication(owner, recruit.getId(), application.getId(), "accepted");
        assertThat(service.listGroups(member).getFirst().members()).hasSize(2);
        StudyDtos.Record created = service.createRecord(request(group, null, owner.getId(), member.getId()), owner);
        em.flush(); em.clear();
        StudyDtos.Record reloaded = service.getRecord(created.id(), member);
        assertThat(reloaded.attendance()).hasSize(2);
        assertThat(reloaded.canManage()).isFalse();
        assertThat(service.listRecords(LocalDate.now(), LocalDate.now(), Long.valueOf(group.id()), member.getId(), member)).hasSize(1);
    }

    @Test void onlyOwnerOrAdminCanManage() {
        assertThatThrownBy(() -> service.createGroup(new StudyDtos.GroupRequest(recruit.getId(), "Other"), outsider)).isInstanceOf(CustomException.class);
        StudyDtos.Group group = group();
        assertThatThrownBy(() -> service.createRecord(request(group, null, owner.getId()), outsider)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> recruitment.getRecruitApplications(outsider, recruit.getId())).isInstanceOf(CustomException.class);
        assertThat(recruitment.getRecruitApplications(owner, recruit.getId())).hasSize(1);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = UserRole.class, names = {"STAFF", "SUPER_ADMIN"})
    void administratorsManageOtherOwnersRecruitmentAndAttendance(UserRole role) {
        User admin = user("Administrator", true, role);
        assertThat(recruitment.getRecruitApplications(admin, recruit.getId())).hasSize(1);
        recruitment.decideApplication(admin, recruit.getId(), application.getId(), "accepted");
        StudyDtos.Group group = service.createGroup(new StudyDtos.GroupRequest(recruit.getId(), "Managed group"), admin);
        assertThat(group.canManage()).isTrue();
        assertThat(group.members()).extracting(StudyDtos.Member::userId).containsExactly(owner.getId().toString(), member.getId().toString());
        StudyDtos.Record record = service.createRecord(request(group, null, owner.getId(), member.getId()), owner);
        assertThat(service.getRecord(record.id(), admin).canManage()).isTrue();
        StudyDtos.RecordRequest changed = new StudyDtos.RecordRequest(Long.valueOf(group.id()), "Admin correction", LocalDate.now(), "Corrected attendance",
                List.of(new StudyDtos.AttendanceRequest(owner.getId(), "present"), new StudyDtos.AttendanceRequest(member.getId(), "late")), record.version());
        StudyDtos.Record updated = service.updateRecord(record.id(), changed, admin);
        assertThat(updated.title()).isEqualTo("Admin correction");
        assertThat(updated.attendance()).extracting(StudyDtos.Attendance::status).containsExactly("present", "late");
        assertThat(service.listGroups(admin)).allMatch(StudyDtos.Group::canManage);
        assertThatThrownBy(() -> recruitment.deleteRecruit(admin, recruit.getId())).isInstanceOf(CustomException.class);
    }

    @Test void rejectsUnverifiedAndInvalidRosters() {
        assertThatThrownBy(() -> service.listGroups(user("Unverified", false))).isInstanceOf(CustomException.class);
        StudyDtos.Group group = group();
        assertThatThrownBy(() -> service.createRecord(request(group, null, outsider.getId()), owner)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.createRecord(request(group, null, owner.getId(), owner.getId()), owner)).isInstanceOf(CustomException.class);
    }

    @Test void pastRosterSurvivesMembershipChangeAndStaleEditsFail() {
        recruitment.decideApplication(owner, recruit.getId(), application.getId(), "accepted");
        StudyDtos.Group group = group();
        StudyDtos.Record record = service.createRecord(request(group, null, owner.getId(), member.getId()), owner);
        recruitment.decideApplication(owner, recruit.getId(), application.getId(), "rejected");
        StudyDtos.Record updated = service.updateRecord(record.id(), request(group, record.version(), owner.getId(), member.getId()), owner);
        assertThat(updated.attendance()).hasSize(2);
        assertThat(updated.version()).isGreaterThan(record.version());
        assertThatThrownBy(() -> service.updateRecord(record.id(), request(group, record.version(), owner.getId(), member.getId()), owner)).isInstanceOf(CustomException.class);
    }

    @Test void groupCreationIsIdempotentAndDeletionProtectsHistory() {
        assertThat(group().id()).isEqualTo(group().id());
        assertThatThrownBy(() -> recruitment.deleteRecruit(owner, recruit.getId())).isInstanceOf(CustomException.class);
    }

    @Test void validatesDatesAndAttendanceStatus() {
        StudyDtos.RecordRequest invalid = new StudyDtos.RecordRequest(1L, "", LocalDate.now().plusDays(1), "",
            List.of(new StudyDtos.AttendanceRequest(-1L, "forged")), null);
        assertThat(validator.validate(invalid)).hasSizeGreaterThanOrEqualTo(5);
        assertThatThrownBy(() -> service.listRecords(LocalDate.now().minusDays(100), LocalDate.now(), null, null, owner)).isInstanceOf(CustomException.class);
    }

    @Test void admissionChecksCapacityAndCannotBeSelfApproved() {
        assertThatThrownBy(() -> recruitment.decideApplication(member, recruit.getId(), application.getId(), "accepted")).isInstanceOf(CustomException.class);
        recruitment.decideApplication(owner, recruit.getId(), application.getId(), "accepted");
        RecruitApplication extra = applications.saveAndFlush(RecruitApplication.builder().recruitPost(recruit).user(outsider)
            .role("Member").body("Join").status("submitted").submittedAt(LocalDateTime.now()).build());
        assertThatThrownBy(() -> recruitment.decideApplication(owner, recruit.getId(), extra.getId(), "accepted")).isInstanceOf(CustomException.class);
    }

    @Test void httpRequiresAuthenticationAndValidatesPayloads() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/study/groups"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is4xxClientError());
        String token = tokens.createToken(owner.getId().toString(), java.util.Map.of("role", "ROLE_USER"), 60000);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/study/groups")
            .header("Authorization", "Bearer " + token).contentType("application/json")
            .content("{\"recruitId\":\"" + recruit.getId() + "\",\"name\":\"API group\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.canManage").value(true));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/study/records")
            .header("Authorization", "Bearer " + token).contentType("application/json")
            .content("{\"groupId\":1,\"title\":\"Week\",\"date\":\"2999-01-01\",\"content\":\"Notes\",\"attendance\":[null]}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        String otherToken = tokens.createToken(outsider.getId().toString(), java.util.Map.of("role", "ROLE_ADMIN"), 60000);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/study/groups")
            .header("Authorization", "Bearer " + otherToken).contentType("application/json")
            .content("{\"recruitId\":\"" + recruit.getId() + "\",\"name\":\"Forged owner\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
}
