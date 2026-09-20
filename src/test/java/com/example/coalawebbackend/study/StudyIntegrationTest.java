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
