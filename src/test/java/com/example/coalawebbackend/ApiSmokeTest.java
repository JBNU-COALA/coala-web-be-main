package com.example.coalawebbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.coalawebbackend.api.auth.service.EmailVerificationCodeStore;
import com.example.coalawebbackend.api.auth.service.EmailVerificationMailService;
import com.example.coalawebbackend.common.jwt.LogoutTokenStore;
import com.example.coalawebbackend.common.jwt.RefreshTokenStore;
import com.example.coalawebbackend.domain.user.entity.UserRole;
import com.example.coalawebbackend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:coala-api-smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.port=1",
        "spring.data.redis.password=",
        "jwt.secret=test-jwt-secret-for-api-smoke",
        "github.api.base-url=http://127.0.0.1:1",
        "app.seed.dev-account.enabled=true",
        "app.security.swagger-enabled=true",
        "app.storage.root-path=build/test-uploads"
})
class ApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmailVerificationCodeStore emailVerificationCodeStore;

    @MockitoBean
    private EmailVerificationMailService emailVerificationMailService;

    @MockitoBean
    private RefreshTokenStore refreshTokenStore;

    @MockitoBean
    private LogoutTokenStore logoutTokenStore;

    @BeforeEach
    void setUp() {
        given(emailVerificationCodeStore.validate(anyString(), anyString())).willReturn(true);
        given(refreshTokenStore.validate(anyString(), anyString())).willReturn(true);
        given(logoutTokenStore.isBlacklisted(anyString())).willReturn(false);
    }

    @Test
    void allApiEndpointsRespond() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String email = "api-smoke-" + suffix + "@jbnu.ac.kr";
        String password = "P@ssw0rd!";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "name": "Api Smoke",
                                  "nickname": "smoke%s",
                                  "birthDate": "2000-01-01",
                                  "gender": "MALE",
                                  "department": "Computer Science",
                                  "studentId": "%s",
                                  "grade": 3,
                                  "githubId": "smoke%s",
                                  "academicStatus": "ENROLLED"
                                }
                                """.formatted(email, password, suffix, numericSuffix(suffix), suffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.verified").value(false));

        mockMvc.perform(post("/api/auth/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/email-verification/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "code": "123456"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode loginJson = readJson(loginResult);
        String refreshToken = loginJson.get("refreshToken").asText();
        long smokeUserId = loginJson.get("user").get("id").asLong();
        userRepository.findById(smokeUserId).ifPresent(user -> {
            user.grantRole(UserRole.SUPER_ADMIN);
            userRepository.save(user);
        });

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String accessToken = readJson(refreshResult).get("accessToken").asText();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/users/{userId}", smokeUserId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Api Smoke"))
                .andExpect(jsonPath("$.isMe").value(true));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(patch("/api/admin/users/{userId}/role", smokeUserId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "SUPER_ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPER_ADMIN"));

        mockMvc.perform(get("/api/services")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].id").value("algo-room"));

        MvcResult serviceResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke service",
                                  "category": "tool",
                                  "summary": "Smoke service",
                                  "url": "https://example.com",
                                  "tags": ["smoke"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Smoke service"))
                .andReturn();
        String createdServiceId = readJson(serviceResult).get("id").asText();

        mockMvc.perform(get("/api/services/{serviceId}", createdServiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdServiceId));

        mockMvc.perform(patch("/api/services/{serviceId}", createdServiceId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke service updated",
                                  "category": "tool",
                                  "summary": "Smoke service updated",
                                  "url": "https://example.com/updated",
                                  "tags": ["smoke", "updated"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Smoke service updated"))
                .andExpect(jsonPath("$.url").value("https://example.com/updated"));

        mockMvc.perform(get("/api/services/instances/applications")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("jc-002"));

        MvcResult applicationResult = mockMvc.perform(post("/api/services/instances/applications")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instanceType": "small",
                                  "duration": "1 month",
                                  "purpose": "smoke"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();
        String createdApplicationId = readJson(applicationResult).get("id").asText();

        mockMvc.perform(patch("/api/services/instances/applications/{applicationId}", createdApplicationId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instanceType": "medium",
                                  "duration": "3 months",
                                  "purpose": "smoke updated",
                                  "status": "approved",
                                  "adminNote": "approved by smoke test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdApplicationId))
                .andExpect(jsonPath("$.status").value("approved"))
                .andExpect(jsonPath("$.adminNote").value("approved by smoke test"));

        MvcResult inquiryResult = mockMvc.perform(post("/api/services/instances/inquiries")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke inquiry",
                                  "content": "Smoke inquiry content",
                                  "author": "Smoke"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Smoke inquiry"))
                .andReturn();
        String inquiryId = readJson(inquiryResult).get("id").asText();

        mockMvc.perform(get("/api/services/instances/inquiries")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(inquiryId));

        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));

        MvcResult infoResult = mockMvc.perform(post("/api/info")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "filter": "resource",
                                  "tag": "자료",
                                  "title": "Smoke info",
                                  "meta": "테스트",
                                  "sourceName": "Smoke",
                                  "sourceDate": "2026-05-01",
                                  "content": "Smoke info content",
                                  "imageUrl": "https://example.com/info.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Smoke info"))
                .andReturn();
        long infoId = readJson(infoResult).get("id").asLong();

        mockMvc.perform(get("/api/info/{infoId}", infoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(infoId));

        mockMvc.perform(post("/api/info/{infoId}/bookmarks", infoId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarkCount").value(1));

        mockMvc.perform(patch("/api/info/{infoId}", infoId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "filter": "news",
                                  "tag": "소식",
                                  "title": "Smoke info updated",
                                  "meta": "수정",
                                  "sourceName": "Smoke",
                                  "sourceDate": "2026-05-02",
                                  "content": "Smoke info content updated",
                                  "imageUrl": "https://example.com/info-updated.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Smoke info updated"));

        mockMvc.perform(get("/api/recruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        mockMvc.perform(get("/api/recruits/{recruitId}", "react-study"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("react-study"));

        MvcResult recruitResult = mockMvc.perform(post("/api/recruits")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke recruit",
                                  "shortDesc": "Smoke recruit short",
                                  "category": "project",
                                  "roles": [{"label": "Backend", "max": 1}],
                                  "techStack": ["Spring Boot"],
                                  "meetingType": "online",
                                  "expectedDuration": "2 weeks",
                                  "tags": ["smoke"],
                                  "detailContent": ["Smoke detail"],
                                  "processList": ["Smoke process"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Smoke recruit"))
                .andReturn();
        String recruitId = readJson(recruitResult).get("id").asText();

        mockMvc.perform(post("/api/recruits/{recruitId}/comments", recruitId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Smoke recruit comment"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Smoke recruit comment"));

        mockMvc.perform(get("/api/recruits/{recruitId}/comments", recruitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/recruits/{recruitId}/applications", recruitId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "Backend",
                                  "body": "Smoke application"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recruitId").value(recruitId));

        mockMvc.perform(get("/api/recruits/applications/me")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/recruits/{recruitId}/applications", recruitId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/recruits/{recruitId}/bookmarks", recruitId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarks").value(1));

        mockMvc.perform(get("/api/github/public-activity")
                        .param("username", "octocat")
                        .param("limit", "3"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardName": "Smoke Board %s",
                                  "boardType": "NORMAL",
                                  "description": "Smoke board"
                                }
                                """.formatted(suffix)))
                .andExpect(status().isForbidden());

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardName": "Smoke Board %s",
                                  "boardType": "NORMAL",
                                  "description": "Smoke board"
                                }
                                """.formatted(suffix)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").exists())
                .andReturn();
        long boardId = readJson(boardResult).get("boardId").asLong();

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/boards")
                        .param("isActive", "true"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/boards/{boardId}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(boardId));

        mockMvc.perform(patch("/api/boards/{boardId}", boardId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardName": "Smoke Board Updated %s",
                                  "description": "Smoke board updated",
                                  "isActive": true
                                }
                                """.formatted(suffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UPDATED"));

        MockMultipartFile smokeImage = new MockMultipartFile(
                "file",
                "smoke.png",
                "image/png",
                new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A}
        );
        MvcResult attachmentResult = mockMvc.perform(multipart("/api/attachments/images")
                        .file(smokeImage)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachmentId").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.status").value("TEMP"))
                .andReturn();
        long attachmentId = readJson(attachmentResult).get("attachmentId").asLong();
        String attachmentUrl = readJson(attachmentResult).get("url").asText();

        MvcResult postResult = mockMvc.perform(post("/api/boards/{boardId}/posts", boardId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke post",
                                  "content": "Smoke post content\\n\\n![smoke](%s)",
                                  "attachmentIds": [%d],
                                  "thumbnailAttachmentId": %d
                                }
                                """.formatted(attachmentUrl, attachmentId, attachmentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").exists())
                .andReturn();
        long postId = readJson(postResult).get("postId").asLong();

        mockMvc.perform(get("/api/attachments/{attachmentId}/download", attachmentId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/boards/{boardId}/posts", boardId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/boards/{boardId}/posts/{postId}", boardId, postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId));

        mockMvc.perform(get("/api/admin/posts")
                        .header("Authorization", bearer(accessToken))
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId").value(postId));

        mockMvc.perform(patch("/api/posts/{postId}", postId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Smoke post updated",
                                  "content": "Smoke post content updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId));

        MvcResult commentResult = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Smoke comment"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").exists())
                .andReturn();
        long commentId = readJson(commentResult).get("commentId").asLong();

        MvcResult replyResult = mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId)
                        .header("Authorization", bearer(accessToken))
                        .header("X-Forwarded-For", "203.0.113.20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Smoke reply"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCommentId").value(commentId))
                .andReturn();
        long replyId = readJson(replyResult).get("commentId").asLong();

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].replies[0].commentId").value(replyId));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}/replies", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commentId").value(replyId));

        mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Smoke comment updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(commentId));

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/likes", postId, commentId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));

        mockMvc.perform(post("/api/posts/{postId}/comments/{commentId}/likes", postId, commentId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));

        MvcResult resourceResult = mockMvc.perform(post("/api/posts/{postId}/resources", postId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fileName": "smoke.pdf",
                                  "fileUrl": "https://example.com/smoke.pdf",
                                  "fileType": "PDF",
                                  "fileSize": 1024
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId").exists())
                .andReturn();
        long resourceId = readJson(resourceResult).get("resourceId").asLong();

        mockMvc.perform(get("/api/posts/{postId}/resources", postId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));

        mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));

        mockMvc.perform(delete("/api/posts/{postId}/resources/{resourceId}", postId, resourceId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/admin/moderation/posts/{postId}/lock", postId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "smoke lock"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/admin/moderation/posts/{postId}/unlock", postId)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "smoke unlock"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").exists());

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/posts/{postId}", postId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/info/{infoId}", infoId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/boards/{boardId}", boardId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/services/{serviceId}", createdServiceId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services/{serviceId}", createdServiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("운영중지"));

        MvcResult openApiResult = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode swaggerPaths = readJson(openApiResult).path("paths");
        assertThat(swaggerPaths.has("/api/users")).isTrue();
        assertThat(swaggerPaths.has("/api/services/instances/inquiries")).isTrue();
        assertThat(swaggerPaths.has("/api/info")).isTrue();
        assertThat(swaggerPaths.has("/api/recruits")).isTrue();
        assertThat(swaggerPaths.has("/api/posts/{postId}/comments/{commentId}/replies")).isTrue();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String numericSuffix(String suffix) {
        return suffix.chars()
                .map(character -> character % 10)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
