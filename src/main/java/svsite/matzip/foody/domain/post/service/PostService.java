package svsite.matzip.foody.domain.post.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.response.MarkersResponseDto;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  public List<MarkersResponseDto> getAllMarkers(User user) {
    return postRepository.getAllMarkers(user).stream()
        .map(MarkersResponseDto::from)
        .toList();
  }

  public PostResponseDto createPost(CreatePostDto createPostDto, User user) {
    Post post = Post.create(createPostDto, user);
    postRepository.save(post);

    return PostResponseDto.from(post);
  }
}
