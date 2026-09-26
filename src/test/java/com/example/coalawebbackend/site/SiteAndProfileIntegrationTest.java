package com.example.coalawebbackend.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.coalawebbackend.api.info.service.InfoArticleService;
import com.example.coalawebbackend.api.recruit.service.RecruitService;
import com.example.coalawebbackend.api.site.dto.SiteBannerRequest;
import com.example.coalawebbackend.api.site.service.SiteBannerService;
import com.example.coalawebbackend.api.study.StudyDtos;
import com.example.coalawebbackend.api.study.StudyService;
import com.example.coalawebbackend.api.users.dto.UserActivityItemResponse;
import com.example.coalawebbackend.api.users.service.UserActivityService;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.common.jwt.LogoutTokenStore;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.entity.BoardType;
import com.example.coalawebbackend.domain.info.entity.InfoArticle;
import com.example.coalawebbackend.domain.info.entity.InfoCategory;
import com.example.coalawebbackend.domain.info.repository.InfoArticleRepository;
import com.example.coalawebbackend.domain.instance.entity.DomainApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceApplication;
import com.example.coalawebbackend.domain.instance.entity.InstanceSpec;
import com.example.coalawebbackend.domain.instance.entity.ServiceInquiry;
import com.example.coalawebbackend.domain.memberservice.entity.MemberService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.recruit.entity.RecruitApplication;
import com.example.coalawebbackend.domain.recruit.entity.RecruitPost;
import com.example.coalawebbackend.domain.recruit.repository.RecruitBookmarkRepository;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.example.coalawebbackend.infra.storage.FileStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SiteAndProfileIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired SiteBannerService banners;
    @Autowired UserActivityService overview;
    @Autowired StudyService study;
    @Autowired RecruitService recruits;
    @Autowired InfoArticleService info;
    @Autowired InfoArticleRepository articles;
    @Autowired RecruitBookmarkRepository recruitBookmarks;
    @Autowired EntityManager em;
    @MockitoBean LogoutTokenStore logoutTokens;
    @MockitoBean FileStorage storage;
    private final ObjectMapper json = new ObjectMapper();
    User owner;
    User other;
    User staff;

    @BeforeEach
    void setup() {
        owner = user(UserRole.USER, true);
        other = user(UserRole.USER, true);
        staff = user(UserRole.STAFF, true);
    }

    User user(UserRole role, boolean verified) {
        String id = UUID.randomUUID().toString();
        return users.saveAndFlush(User.builder().email(id + "@example.test").password("test-only")
                .name("Test member").githubId(id).studentId(id.substring(0, 18)).department("CS")
                .academicStatus(AcademicStatus.ENROLLED).verified(verified).role(role).build());
    }

    RequestPostProcessor as(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(user.getId().toString(), null, List.of()));
    }

    SiteBannerRequest banner(String title, int order, boolean enabled) {
        return new SiteBannerRequest(title, null, null, null, "/about", "Open", "green", order, enabled);
    }

    @Test
    void publicBannerListIsEnabledSortedAndMayBeEmpty() throws Exception {
        banners.create(staff, banner("Hidden", 0, false));
        mvc.perform(get("/api/site/banners")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        banners.create(staff, banner("Later", 2, true));
        banners.create(staff, banner("First", 1, true));
        banners.create(staff, banner("Second", 1, true));
        mvc.perform(get("/api/site/banners")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("First"))
                .andExpect(jsonPath("$[1].title").value("Second"))
                .andExpect(jsonPath("$[2].title").value("Later"));
        mvc.perform(get("/api/admin/banners").with(as(staff))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"STAFF", "SUPER_ADMIN"})
    void moderatorBannerCrudPersistsAndSupportsHideAll(UserRole role) throws Exception {
        var admin = user(role, true);
        var created = mvc.perform(post("/api/admin/banners").with(as(admin)).contentType("application/json")
                        .content(json.writeValueAsString(banner("Created", 0, true))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.imageUrl").value(""))
                .andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(patch("/api/admin/banners/{id}", id).with(as(admin)).contentType("application/json")
                        .content(json.writeValueAsString(banner("Updated", 3, false))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
        em.flush(); em.clear();
        mvc.perform(get("/api/site/banners")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/admin/banners/{id}", id).with(as(admin))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/admin/banners/{id}", id).with(as(admin))).andExpect(status().isNotFound());
    }

    @Test
    void bannerManagementRequiresPersistedModeratorRoleAndValidUrls() throws Exception {
        long id = banners.create(staff, banner("Existing", 0, true)).id();
        String body = json.writeValueAsString(banner("Changed", 0, true));
        mvc.perform(get("/api/admin/banners")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/banners").with(as(owner))).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/banners").with(as(owner)).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/admin/banners/{id}", id).with(as(owner)).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/admin/banners/{id}", id).with(as(owner))).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/banners").with(as(staff)).contentType("application/json")
                .content(body.replace("/about", "//evil.test"))).andExpect(status().isBadRequest());
    }

    @Test
    void overviewUsesLinkedRecordsWithoutDisclosingPrivateOrAnonymousItems() throws Exception {
        var normal = board(BoardType.NORMAL, true);
        boardPost(normal, "Public");
        boardPost(board(BoardType.ANONYMOUS, true), "Anonymous");
        boardPost(board(BoardType.NORMAL, false), "Inactive board");
        var unclassified = board(BoardType.NORMAL, true);
        unclassified.updateCategoryKey(null);
        boardPost(unclassified, "Unclassified board");
        var hidden = boardPost(normal, "Hidden");
        hidden.hide();
        var article = article();
        service("Public", "Public service");
        service("Private", "Private service");
        var recruit = recruit(owner);
        var group = study.createGroup(new StudyDtos.GroupRequest(recruit.getId(), "Group"), owner);
        em.persist(RecruitApplication.builder().recruitPost(recruit).user(owner).role("Guest").body("Private application")
                .status("submitted").submittedAt(LocalDateTime.now()).build());
        recruits.bookmark(recruit.getId(), owner.getId().toString());
        em.persist(InstanceApplication.builder().id("instance-test").user(owner).applicantName("Owner").studentId("123")
                .keyEmail("private@example.test").instanceType("small").purpose("Private instance").duration("1 month")
                .requestedAt(LocalDate.now()).status("submitted").specs(InstanceSpec.forType("small")).build());
        em.persist(DomainApplication.builder().id("domain-test").user(owner).applicantName("Owner").studentId("123")
                .contactEmail("private@example.test").serviceName("Private domain").desiredAddress("private")
                .requestedDomain("private.example.test").repositoryUrl("https://example.test/private")
                .purpose("Private domain purpose").requestedAt(LocalDate.now()).status("submitted").build());
        var record = study.createRecord(new StudyDtos.RecordRequest(Long.valueOf(group.id()), "Linked study", LocalDate.now(),
                "Notes", List.of(new StudyDtos.AttendanceRequest(owner.getId(), "present"),
                new StudyDtos.AttendanceRequest(other.getId(), "late")), null), owner);
        em.flush(); em.clear();

        var own = overview.getOverview(owner.getId(), owner.getId());
        assertThat(own.isSelf()).isTrue();
        assertThat(own.counts().authoredPosts()).isEqualTo(5);
        assertThat(own.counts().studyRecords()).isEqualTo(1);
        assertThat(own.counts().studyGroups()).isEqualTo(1);
        assertThat(own.counts().pendingRecruitApplications()).isEqualTo(1);
        assertThat(own.counts().savedRecruits()).isEqualTo(1);
        assertThat(own.counts().instanceApplications()).isEqualTo(1);
        assertThat(own.counts().domainApplications()).isEqualTo(1);
        assertThat(own.items()).extracting(UserActivityItemResponse::kind)
                .contains("board", "info", "recruit", "service", "study", "instance", "domain", "recruit-application");

        var visible = overview.getOverview(owner.getId(), other.getId());
        assertThat(visible.isSelf()).isFalse();
        assertThat(visible.counts().authoredPosts()).isEqualTo(1);
        assertThat(visible.counts().services()).isEqualTo(1);
        assertThat(visible.counts().recruitApplications()).isNull();
        assertThat(visible.counts().savedRecruits()).isNull();
        assertThat(visible.counts().instanceApplications()).isNull();
        assertThat(visible.counts().domainApplications()).isNull();
        assertThat(visible.items()).extracting(UserActivityItemResponse::title)
                .contains("Public", article.getTitle(), "Linked study")
                .doesNotContain("Anonymous", "Inactive board", "Unclassified board", "Hidden", "Private service", "Private domain");
        assertThat(overview.getOverview(owner.getId(), staff.getId()).counts().recruitApplications()).isNull();
        assertThat(overview.getOverview(other.getId(), other.getId()).counts().studyRecords()).isEqualTo(1);
        assertThat(overview.getOverview(other.getId(), other.getId()).counts().studyGroups()).isZero();
        assertThat(study.listRecords(LocalDate.now(), LocalDate.now(), null, owner.getId(), other))
                .singleElement().satisfies(item -> {
                    assertThat(item.id()).isEqualTo(record.id());
                    assertThat(item.attendance()).hasSize(2);
                    assertThat(item.canManage()).isFalse();
                });
        mvc.perform(get("/api/users/me/overview").with(as(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.isSelf").value(true)).andExpect(jsonPath("$.counts.studyRecords").value(1));
        mvc.perform(get("/api/users/{id}/overview", owner.getId()).with(as(other))).andExpect(status().isOk())
                .andExpect(jsonPath("$.isSelf").value(false));
    }

    @Test
    void overviewRejectsAnonymousUnverifiedAndMissingProfiles() throws Exception {
        mvc.perform(get("/api/users/me/overview")).andExpect(status().isUnauthorized());
        var unverified = user(UserRole.USER, false);
        mvc.perform(get("/api/users/me/overview").with(as(unverified))).andExpect(status().isForbidden());
        mvc.perform(get("/api/users/{id}/overview", Long.MAX_VALUE).with(as(owner))).andExpect(status().isNotFound());
        assertThatThrownBy(() -> study.listRecords(LocalDate.now(), LocalDate.now(), null, -1L, owner))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void recruiterBookmarksAreScopedIdempotentAndCountsFollowRemoval() throws Exception {
        var recruit = recruit(owner);
        assertThat(recruits.bookmark(recruit.getId(), owner.getId().toString()).bookmarks()).isEqualTo(1);
        assertThat(recruits.bookmark(recruit.getId(), owner.getId().toString()).bookmarks()).isEqualTo(1);
        assertThat(recruits.bookmark(recruit.getId(), other.getId().toString()).bookmarks()).isEqualTo(2);
        mvc.perform(get("/api/recruits/bookmarks/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/recruits/bookmarks/me").with(as(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(recruit.getId()));
        mvc.perform(delete("/api/recruits/{id}/bookmarks", recruit.getId()).with(as(owner))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/recruits/{id}/bookmarks", recruit.getId()).with(as(owner))).andExpect(status().isNoContent());
        em.flush(); em.clear();
        assertThat(recruits.getMyBookmarks(owner.getId().toString())).isEmpty();
        assertThat(recruits.getMyBookmarks(other.getId().toString())).singleElement()
                .satisfies(item -> assertThat(item.bookmarks()).isEqualTo(1));
        assertThat(recruitBookmarks.countByUser_Id(other.getId())).isEqualTo(1);
    }

    @Test
    void infoBookmarksPersistToggleAndNeverReuseUnownedLegacyCounters() throws Exception {
        var article = article();
        article.increaseBookmarkCount();
        assertThat(info.getArticle(article.getId()).bookmarkCount()).isZero();
        mvc.perform(post("/api/info/{id}/bookmarks", article.getId()).with(as(owner)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookmarkedByMe").value(true))
                .andExpect(jsonPath("$.bookmarkCount").value(1));
        em.flush(); em.clear();
        mvc.perform(get("/api/info/{id}", article.getId()).with(as(owner)))
                .andExpect(jsonPath("$.bookmarkedByMe").value(true));
        mvc.perform(get("/api/info/{id}", article.getId()).with(as(other)))
                .andExpect(jsonPath("$.bookmarkedByMe").value(false));
        mvc.perform(get("/api/info/bookmarks/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/info/bookmarks/me").with(as(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/info/bookmarks/me").with(as(other))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        info.bookmarkArticle(other, article.getId());
        mvc.perform(post("/api/info/{id}/bookmarks", article.getId()).with(as(owner)))
                .andExpect(jsonPath("$.bookmarkedByMe").value(false)).andExpect(jsonPath("$.bookmarkCount").value(1));
        info.deleteArticle(owner, article.getId());
        em.flush();
        assertThat(info.getMyBookmarks(other)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"instances", "domains"})
    void inquiriesAreOwnerLinkedAndModeratorsCanReadAndAnswerTheFullText(String type) throws Exception {
        String path = "/api/services/" + type + "/inquiries";
        String content = "Long inquiry detail. ".repeat(20);
        var created = mvc.perform(post(path).with(as(owner)).contentType("application/json")
                        .content(json.writeValueAsString(java.util.Map.of("title", "Question", "content", content,
                                "author", "Forged author"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.author").value(owner.getName()))
                .andExpect(jsonPath("$.authorId").value(owner.getId())).andReturn();
        String id = json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        String answer = "{\"status\":\"answered\",\"reply\":\"The answer\"}";
        mvc.perform(patch(path + "/" + id).contentType("application/json").content(answer))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch(path + "/" + id).with(as(owner)).contentType("application/json").content(answer))
                .andExpect(status().isForbidden());
        mvc.perform(patch(path + "/" + id).with(as(other)).contentType("application/json").content(answer))
                .andExpect(status().isForbidden());
        mvc.perform(patch(path + "/" + id).with(as(staff)).contentType("application/json")
                .content("{\"status\":\"answered\",\"reply\":\" \"}")).andExpect(status().isBadRequest());
        mvc.perform(patch(path + "/" + id).with(as(staff)).contentType("application/json")
                .content("{\"status\":\"forged\",\"reply\":\"Answer\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch(path + "/" + id).with(as(staff)).contentType("application/json").content(answer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("answered"))
                .andExpect(jsonPath("$.content").value(content.trim()))
                .andExpect(jsonPath("$.reply").value("The answer")).andExpect(jsonPath("$.answeredAt").isString());
        em.flush(); em.clear();
        mvc.perform(get(path).with(as(owner))).andExpect(status().isOk()).andExpect(jsonPath("$[0].reply").value("The answer"));
        mvc.perform(get(path).with(as(other))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get(path).with(as(staff))).andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value(content.trim()));
        String wrongPath = "/api/services/" + (type.equals("instances") ? "domains" : "instances") + "/inquiries/" + id;
        mvc.perform(patch(wrongPath).with(as(staff)).contentType("application/json").content(answer))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerlessLegacyInquiriesAreNotAssignedByDisplayName() throws Exception {
        em.persist(ServiceInquiry.builder().id("dom-inq-legacy").title("Legacy question").summary("Summary")
                .content("Private legacy content").author(owner.getName()).status("open")
                .statusClass("status--pending").createdDate(LocalDate.now()).build());
        mvc.perform(get("/api/services/domains/inquiries").with(as(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/services/domains/inquiries").with(as(staff))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void boardClassificationSurvivesRenameAndControlsNoticeWrites() throws Exception {
        var created = mvc.perform(post("/api/boards").with(as(staff)).contentType("application/json")
                        .content("{\"boardName\":\"Announcements\",\"boardType\":\"NORMAL\",\"categoryKey\":\"notice\"}"))
                .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).get("boardId").asLong();
        mvc.perform(patch("/api/boards/{id}", id).with(as(staff)).contentType("application/json")
                .content("{\"boardName\":\"Renamed board\",\"description\":\"Changed\",\"isActive\":true}"))
                .andExpect(status().isOk());
        em.flush(); em.clear();
        mvc.perform(get("/api/boards/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.boardName").value("Renamed board"))
                .andExpect(jsonPath("$.categoryKey").value("notice"));
        mvc.perform(post("/api/boards/{id}/posts", id).with(as(owner)).contentType("application/json")
                .content("{\"title\":\"Forged notice\",\"content\":\"Body\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANONYMOUS", "RECRUIT"})
    void categoriesCannotBeAssignedToNonNormalBoards(String type) throws Exception {
        mvc.perform(post("/api/boards").with(as(staff)).contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("boardName", "Invalid category", "boardType", type, "categoryKey", "notice"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newNormalBoardDefaultsToFreeAndRejectsUnknownCategories() throws Exception {
        var created = mvc.perform(post("/api/boards").with(as(staff)).contentType("application/json")
                        .content("{\"boardName\":\"General discussion\",\"boardType\":\"NORMAL\"}"))
                .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).get("boardId").asLong();
        mvc.perform(get("/api/boards/{id}", id)).andExpect(jsonPath("$.categoryKey").value("free"));
        mvc.perform(patch("/api/boards/{id}", id).with(as(staff)).contentType("application/json")
                .content("{\"boardName\":\"General discussion\",\"categoryKey\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    Board board(BoardType type, boolean active) {
        var board = Board.builder().name(UUID.randomUUID().toString()).type(type).isActive(active).user(owner).build();
        em.persist(board);
        return board;
    }

    Post boardPost(Board board, String title) {
        var post = Post.create(title, "Body", board, owner);
        em.persist(post);
        return post;
    }

    InfoArticle article() {
        return articles.saveAndFlush(InfoArticle.builder().author(owner).category(InfoCategory.NEWS).tag("News")
                .title("Owned info").meta("Summary").sourceName("Owner").sourceDate(LocalDate.now())
                .content("Body").imageUrl("").build());
    }

    RecruitPost recruit(User author) {
        var recruit = RecruitPost.builder().id(UUID.randomUUID().toString()).author(author).title("Recruit")
                .shortDesc("Summary").category("study").status("open").host("Owner").hostInitials("O")
                .hostTone("mint").hostRole("host").maxMembers(3).meetingType("online").expectedDuration("8 weeks").build();
        em.persist(recruit);
        return recruit;
    }

    void service(String visibility, String title) {
        em.persist(MemberService.builder().id(UUID.randomUUID().toString()).ownerUser(owner).owner("Owner")
                .title(title).category("Tool").summary("Summary").url("https://example.test").githubUrl("")
                .imageUrl("").status("active").audience("Members").visibility(visibility).period("Current")
                .description("Description").build());
    }
}
