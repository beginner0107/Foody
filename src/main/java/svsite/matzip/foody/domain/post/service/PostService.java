package svsite.matzip.foody.domain.post.service;

import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.POST_NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.image.entity.Image;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.request.UpdatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.PostRepository;
import svsite.matzip.foody.global.exception.support.CustomException;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  @Transactional(readOnly = true)
  public List<MarkersResponseDto> getAllMarkers(User user) {
    return postRepository.getAllMarkers(user).stream()
        .map(MarkersResponseDto::from)
        .toList();
  }

  @Transactional
  public PostResponseDto createPost(CreatePostDto createPostDto, User user) {
    Post post = Post.create(createPostDto, user);
    postRepository.save(post);
    return PostResponseDto.from(post);
  }

  @Transactional
  public PostResponseDto updatePost(long id, UpdatePostDto updatePostDto, User user) {
    Post post = postRepository.findByPostIdAndUser(id, user)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    post.update(updatePostDto);
    post.updateImages(updatePostDto.imageUris().stream()
        .map(uri -> Image.builder().uri(uri).build())
        .toList());

    post = postRepository.save(post);

    return PostResponseDto.from(post);
  }

  @Transactional
  public void deletePost(long id, User user) {
    Post post = postRepository.findByPostIdAndUser(id, user)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    postRepository.delete(post);
  }

  @Transactional(readOnly = true)
  public Page<PostResponseDto> getPosts(PageRequest pageable, User user) {
    return postRepository.findAllRecentPost(pageable, user)
        .map(PostResponseDto::from);
  }

  @Transactional(readOnly = true)
  public PostResponseDto getPostById(long id, User user) {
    Post foundPost = postRepository.findByPostIdAndUser(id, user)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    return PostResponseDto.from(foundPost);
  }

  @Transactional(readOnly = true)
  public Map<Integer, List<PostResponseDto>> getPostsByMonth(int year, int month, User user) {
    List<Post> posts = postRepository.findPostsByMonth(year, month, user);

    return posts.stream()
        .collect(Collectors.groupingBy(
            post -> post.getDate().getDayOfMonth(),
            Collectors.mapping(PostResponseDto::from, Collectors.toList())
        ));
  }

  @Transactional(readOnly = true)
  public Page<PostResponseDto> searchMyPostsByTitleAndAddress(Pageable pageable, String query,
      User user) {
    Page<Post> posts = postRepository.searchByTitleOrAddress(query, user, pageable);
    return posts.map(PostResponseDto::from);
  }
}
