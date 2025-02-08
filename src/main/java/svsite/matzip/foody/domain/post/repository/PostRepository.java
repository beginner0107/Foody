package svsite.matzip.foody.domain.post.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.domain.post.repository.dto.PostMarkersQueryDto;

public interface PostRepository extends JpaRepository<Post, Long> {

  @Query("SELECT new svsite.matzip.foody.domain.post.repository.dto.PostMarkersQueryDto(" +
      "p.id, " +
      "p.latitude, " +
      "p.longitude, " +
      "p.color, " +
      "p.score) " +
      "FROM Post p " +
      "WHERE p.user = :user")
  List<PostMarkersQueryDto> getAllMarkers(@Param("user") User user);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN FETCH p.images i " +
      "WHERE p.user = :user " +
      "AND p.id = :id")
  Optional<Post> findByPostIdAndUser(@Param("id") long id, @Param("user") User user);

  @Query("SELECT p " +
      "FROM Post p " +
      "LEFT JOIN FETCH p.images i " +
      "WHERE p.user = :user " +
      "ORDER BY p.date DESC, i.id ASC")
  Page<Post> findAllRecentPost(Pageable pageable, @Param("user") User user);

  @Query("SELECT p FROM Post p " +
      "WHERE EXTRACT(YEAR FROM p.date) = :year " +
      "AND EXTRACT(MONTH FROM p.date) = :month " +
      "AND p.user = :user")
  List<Post> findPostsByMonth(@Param("year") int year, @Param("month") int month, @Param("user") User user);
}
