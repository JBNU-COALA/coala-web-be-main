package com.example.coalawebbackend.domain.post.service;


import com.example.coalawebbackend.api.post.dto.CreatePostResponse;
import com.example.coalawebbackend.api.post.dto.PostDetailResponse;
import com.example.coalawebbackend.api.post.dto.PostListResponse;
import com.example.coalawebbackend.api.post.dto.PostRequest;
import com.example.coalawebbackend.api.post.dto.UpdatePostResponse;
import com.example.coalawebbackend.common.enums.ErrorCode;
import com.example.coalawebbackend.common.exception.CustomException;
import com.example.coalawebbackend.domain.board.entity.Board;
import com.example.coalawebbackend.domain.board.service.BoardService;
import com.example.coalawebbackend.domain.post.entity.Post;
import com.example.coalawebbackend.domain.post.repository.PostRepository;
import com.example.coalawebbackend.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;

    @Transactional
    public CreatePostResponse createPost(User user, Long boardId, PostRequest request) {
        Board board = boardService.getBoardById(boardId);
        Post post = Post.create(request.getTitle(),request.getContent(), board, user);
        return CreatePostResponse.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getPosts(Long boardId) {
        return postRepository.findByBoardBoardId(boardId)
                .stream()
                .map(PostListResponse::from)
                .toList();
    }

    @Transactional
    public PostDetailResponse getPostDetail(Long boardId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        if (!post.getBoard().getBoardId().equals(boardId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        post.increaseViewCount();
        return PostDetailResponse.from(post);
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, PostRequest request, User user) {
        Post post = getPostById(postId);
        validatePostOwner(post, user);
        post.update(request.getTitle(), request.getContent());
        return UpdatePostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = getPostById(postId);
        validatePostOwner(post, user);
        postRepository.delete(post);
    }


    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(Post post, User user) {
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
