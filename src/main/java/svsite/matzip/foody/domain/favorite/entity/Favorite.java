package svsite.matzip.foody.domain.favorite.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.global.entity.BaseEntity;

@Entity
@Table(name = "favorite",
    uniqueConstraints = @UniqueConstraint(name = "uk_favorite_user_post", columnNames = {"user_id", "post_id"}),
    indexes = {
        @Index(name = "idx_favorite_user", columnList = "user_id"),
        @Index(name = "idx_favorite_post", columnList = "post_id")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Favorite favorite)) return false;
    return id != null && id.equals(favorite.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
