package com.example.coalawebbackend.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.example.coalawebbackend.api.resource.dto.CreateResourceRequest;
import com.example.coalawebbackend.api.resource.dto.ResourceResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.resource.entity.Resource;
import com.example.coalawebbackend.domain.resource.repository.ResourceRepository;
import com.example.coalawebbackend.domain.resource.service.ResourceService;
import com.example.coalawebbackend.domain.user.entity.AcademicStatus;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    private User makeUser(Long id) {
        User user = User.builder()
                .email("test" + id + "@example.com")
                .password("password")
                .name("테스트유저" + id)
                .department("컴퓨터공학과")
                .studentId("2024" + id)
                .academicStatus(AcademicStatus.ENROLLED)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post makePost() {
        return mock(Post.class);
    }

    private Resource makeResource() {
        Post post = makePost();
        given(post.getPostId()).willReturn(1L);

        Resource resource = mock(Resource.class);
        given(resource.getId()).willReturn(1L);
        given(resource.getPost()).willReturn(post);  // 추가
        given(resource.getFileName()).willReturn("file.pdf");
        given(resource.getFileUrl()).willReturn("https://example.com/file.pdf");
        given(resource.getFileType()).willReturn("application/pdf");
        given(resource.getFileSize()).willReturn(1024L);
        return resource;
    }

    // mock 없이 빌더로 생성
    private CreateResourceRequest makeRequest() {
        return CreateResourceRequest.builder()
                .fileName("file.pdf")
                .fileUrl("https://example.com/file.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();
    }

    // ───────────────────────────────────────────
    // createResource
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("createResource")
    class CreateResource {

        @Test
        @DisplayName("post 작성자가 resource를 생성하면 성공한다")
        void success() {
            // given
            User owner = makeUser(1L);
            Post post = makePost();
            given(post.getUser()).willReturn(owner);

            Resource saved = makeResource();
            given(resourceRepository.save(any(Resource.class))).willReturn(saved);

            // when
            ResourceResponse response = resourceService.createResource(post, owner, makeRequest());

            // then
            assertThat(response).isNotNull();
            then(resourceRepository).should().save(any(Resource.class));
        }

        @Test
        @DisplayName("post 작성자가 아닌 사용자가 resource를 생성하면 ACCESS_DENIED 예외가 발생한다")
        void fail_notOwner() {
            // given
            User owner = makeUser(1L);
            User other = makeUser(2L);
            Post post = makePost();
            given(post.getUser()).willReturn(owner);

            // when & then
            assertThatThrownBy(() -> resourceService.createResource(post, other, makeRequest()))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED));

            then(resourceRepository).should(never()).save(any());
        }
    }

    // ───────────────────────────────────────────
    // getResources
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("getResources")
    class GetResources {

        @Test
        @DisplayName("post에 속한 resource 목록을 반환한다")
        void success() {
            // given
            Post post = makePost();
            Resource resource = makeResource();

            given(resourceRepository.findByPostWithFetch(post)).willReturn(List.of(resource));

            // when
            List<ResourceResponse> result = resourceService.getResources(post);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("resource가 없으면 빈 리스트를 반환한다")
        void empty() {
            // given
            Post post = makePost();
            given(resourceRepository.findByPostWithFetch(post)).willReturn(List.of());

            // when
            List<ResourceResponse> result = resourceService.getResources(post);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ───────────────────────────────────────────
    // deleteResource
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("deleteResource")
    class DeleteResource {

        @Test
        @DisplayName("resource 소유자가 삭제하면 성공한다")
        void success() {
            // given
            User owner = makeUser(1L);
            Resource resource = mock(Resource.class);
            given(resource.getUser()).willReturn(owner);
            given(resourceRepository.findById(1L)).willReturn(Optional.of(resource));

            // when
            resourceService.deleteResource(1L, owner);

            // then
            then(resourceRepository).should().delete(resource);
        }

        @Test
        @DisplayName("resource가 존재하지 않으면 RESOURCE_NOT_FOUND 예외가 발생한다")
        void fail_notFound() {
            // given
            User owner = makeUser(1L);
            given(resourceRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> resourceService.deleteResource(999L, owner))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));

            then(resourceRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("resource 소유자가 아닌 사용자가 삭제하면 ACCESS_DENIED 예외가 발생한다")
        void fail_notOwner() {
            // given
            User owner = makeUser(1L);
            User other = makeUser(2L);
            Resource resource = mock(Resource.class);
            given(resource.getUser()).willReturn(owner);
            given(resourceRepository.findById(1L)).willReturn(Optional.of(resource));

            // when & then
            assertThatThrownBy(() -> resourceService.deleteResource(1L, other))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED));

            then(resourceRepository).should(never()).delete(any());
        }
    }

    // ───────────────────────────────────────────
    // getResourceById
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("getResourceById")
    class GetResourceById {

        @Test
        @DisplayName("존재하는 id면 resource를 반환한다")
        void success() {
            // given
            Resource resource = mock(Resource.class);
            given(resourceRepository.findById(1L)).willReturn(Optional.of(resource));

            // when
            Resource result = resourceService.getResourceById(1L);

            // then
            assertThat(result).isEqualTo(resource);
        }

        @Test
        @DisplayName("존재하지 않는 id면 RESOURCE_NOT_FOUND 예외가 발생한다")
        void fail_notFound() {
            // given
            given(resourceRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> resourceService.getResourceById(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
        }
    }
}
