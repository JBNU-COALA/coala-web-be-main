package com.example.coalawebbackend.domain.post.service;


import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostDetailResponse;
import com.example.coalawebbackend.api.post.dto.PostListResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.anonymous.service.AnonymousProfileService;
import com.example.coalawebbackend.domain.attachment.service.AttachmentService;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.entity.BoardType;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.comment.repository.CommentRepository;
import com.example.coalawebbackend.domain.moderation.entity.ContentHistoryAction;
import com.example.coalawebbackend.domain.moderation.entity.PostHistory;
import com.example.coalawebbackend.domain.moderation.repository.PostHistoryRepository;
import com.example.coalawebbackend.domain.moderation.service.ContentSafetyService;
import com.example.coalawebbackend.domain.moderation.service.PermissionService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.entity.PostStatus;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.postlike.repository.PostLikeRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import com.example.coalawebbackend.infra.storage.MarkdownArchiveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostHistoryRepository postHistoryRepository;
    private final PermissionService permissionService;
    private final ContentSafetyService contentSafetyService;
    private final AttachmentService attachmentService;
    private final MarkdownArchiveService markdownArchiveService;
    private final AnonymousProfileService anonymousProfileService;

    @Transactional
    public CreatePostResponse createPost(User user, Long boardId, PostRequest request) {
        Board board = boardService.getBoardById(boardId);
        permissionService.assertCanCreatePost(user, board);
        contentSafetyService.validateMarkdown(request.getTitle());
        contentSafetyService.validateMarkdown(request.getContent());
        if (board.getType() == BoardType.ANONYMOUS) {
            anonymousProfileService.getOrCreateProfile(board, user);
        }
        Post post = Post.create(request.getTitle(), request.getContent(), board, user);
        Post savedPost = postRepository.save(post);
        Long thumbnailAttachmentId = attachmentService.syncPostAttachments(
                user,
                savedPost,
                request.getAttachmentIds(),
                request.getThumbnailAttachmentId());
        savedPost.updateThumbnailAttachmentId(thumbnailAttachmentId);
        markdownArchiveService.savePostSnapshot(savedPost);
        saveHistory(savedPost, user, ContentHistoryAction.CREATED, null);
        return CreatePostResponse.from(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPosts(Long boardId) {
        return getPosts(boardId, null);
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPosts(Long boardId, Long currentUserId) {
        return postRepository.findByBoardBoardIdAndStatusOrderByCreatedAtDesc(boardId, PostStatus.ACTIVE)
                .stream()
                .map(post -> toPostListResponse(post, currentUserId))
                .toList();
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long boardId, Long postId) {
        return getPostDetail(boardId, postId, null);
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long boardId, Long postId, Long currentUserId) {
        Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!post.getBoard().getBoardId().equals(boardId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.increaseViewCount(postId);
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return toPostDetailResponse(updatedPost, currentUserId);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, PostRequest request, User user) {
        Post post = getPostById(postId);
        permissionService.assertCanUpdatePost(user, post);
        contentSafetyService.validateMarkdown(request.getTitle());
        contentSafetyService.validateMarkdown(request.getContent());
        saveHistory(post, user, ContentHistoryAction.UPDATED, "before update");
        if (request.getBoardId() != null && !request.getBoardId().equals(post.getBoard().getBoardId())) {
            Board targetBoard = boardService.getBoardById(request.getBoardId());
            permissionService.assertCanCreatePost(user, targetBoard);
            post.updateBoard(targetBoard);
        }
        post.update(request.getTitle(), request.getContent());
        if (request.getAttachmentIds() != null || request.getThumbnailAttachmentId() != null) {
            Long thumbnailAttachmentId = attachmentService.syncPostAttachments(
                    user,
                    post,
                    request.getAttachmentIds(),
                    request.getThumbnailAttachmentId());
            post.updateThumbnailAttachmentId(thumbnailAttachmentId);
        }
        markdownArchiveService.savePostSnapshot(post);
        return UpdatePostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getPostById(postId);
        permissionService.assertCanUserDeletePost(user, post);
        saveHistory(post, user, ContentHistoryAction.USER_DELETED, "user deleted");
        post.softDelete(user, "사용자 삭제", false);
        attachmentService.markPostAttachmentsDeleted(post, user);
    }


    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    public Post getVisiblePostById(Long postId) {
        return postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private PostListResponse toPostListResponse(Post post, Long currentUserId) {
        boolean mine = isMine(post, currentUserId);
        boolean anonymous = isAnonymousBoard(post);
        return PostListResponse.from(
                post,
                commentRepository.countByPost_PostId(post.getPostId()),
                postLikeRepository.countByPost(post),
                isLikedBy(post, currentUserId),
                resolveDisplayName(post, anonymous),
                anonymous,
                mine);
    }

    private PostDetailResponse toPostDetailResponse(Post post, Long currentUserId) {
        boolean mine = isMine(post, currentUserId);
        boolean anonymous = isAnonymousBoard(post);
        return PostDetailResponse.from(
                post,
                commentRepository.countByPost_PostId(post.getPostId()),
                postLikeRepository.countByPost(post),
                isLikedBy(post, currentUserId),
                resolveDisplayName(post, anonymous),
                anonymous,
                mine);
    }

    private boolean isLikedBy(Post post, Long currentUserId) {
        return currentUserId != null
                && postLikeRepository.existsByUser_IdAndPost_PostId(currentUserId, post.getPostId());
    }

    private boolean isMine(Post post, Long currentUserId) {
        return currentUserId != null && currentUserId.equals(post.getUser().getId());
    }

    private boolean isAnonymousBoard(Post post) {
        return post.getBoard().getType() == BoardType.ANONYMOUS;
    }

    private String resolveDisplayName(Post post, boolean anonymous) {
        if (anonymous) {
            return anonymousProfileService.getDisplayName(post.getBoard(), post.getUser());
        }
        String nickname = post.getUser().getNickname();
        return (nickname != null && !nickname.isBlank()) ? nickname : post.getUser().getName();
    }

    private void saveHistory(Post post, User actor, ContentHistoryAction action, String reason) {
        postHistoryRepository.save(PostHistory.builder()
                .post(post)
                .actor(actor)
                .action(action)
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .reason(reason)
                .build());
    }
}
