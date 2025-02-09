package svsite.matzip.foody.domain.favorite.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.entity.Favorite;
import svsite.matzip.foody.domain.post.entity.Post;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByPostAndUser(Post post, User user);

    @Query("SELECT f FROM Favorite f " +
            "JOIN FETCH f.post p " +
            "LEFT JOIN FETCH p.images i " +
            "WHERE f.user = :user " +
            "ORDER BY p.date DESC")
    Page<Favorite> findFavoritesByUser(@Param("user") User user, Pageable pageable);
}
