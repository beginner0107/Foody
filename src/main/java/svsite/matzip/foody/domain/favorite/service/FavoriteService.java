package svsite.matzip.foody.domain.favorite.service;

import static svsite.matzip.foody.global.exception.errorCode.ErrorCodes.POST_NOT_FOUND;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.api.response.ToggleFavoriteResponseDto;
import svsite.matzip.foody.domain.favorite.entity.Favorite;
import svsite.matzip.foody.domain.favorite.repository.FavoriteRepository;
import svsite.matzip.foody.domain.post.api.dto.response.PostResponseDto;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.PostRepository;
import svsite.matzip.foody.global.exception.support.CustomException;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;

    public ToggleFavoriteResponseDto toggleFavorite(long postId, User user) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        Optional<Favorite> existingFavorite = favoriteRepository.findByPostAndUser(post, user);

        existingFavorite.ifPresentOrElse(
            favoriteRepository::delete,
            () -> {
                Favorite newFavorite = Favorite.builder().post(post).user(user).build();
                favoriteRepository.save(newFavorite);
            }
        );

        boolean isFavorite = existingFavorite.isEmpty();
        return new ToggleFavoriteResponseDto(postId, isFavorite);
    }


    @Transactional(readOnly = true)
    public Page<PostResponseDto> getMyFavoritePosts(Pageable pageable, User user) {
        return favoriteRepository.findFavoritesByUser(user, pageable)
            .map(favorite -> PostResponseDto.fromWithFavorite(favorite.getPost(), true));
    }
}
