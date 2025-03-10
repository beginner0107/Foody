package svsite.matzip.foody.domain.image.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.post.entity.Post;
import svsite.matzip.foody.global.entity.BaseEntity;

@Entity
@Table(name = "image")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String uri;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "post_id")
  Post post;

  public void associateWithPost(Post post) {
    if (this.post == post) {
      return;
    }

    if (this.post != null) {
      this.post.getImages().remove(this);
    }

    this.post = post;

    if (post != null && post.getImages().stream()
        .noneMatch(image -> image.getUri().equals(this.uri))) {
      post.getImages().add(this);
    }
  }

  public void dissociateFromPost() {
    if (this.post != null) {
      this.post.getImages().remove(this);
      this.post = null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Image image)) return false;
    return (id != null ? id.equals(image.id) : uri.equals(image.uri));
  }

  @Override
  public int hashCode() {
    return id != null ? Objects.hash(id) : Objects.hash(uri);
  }
}
