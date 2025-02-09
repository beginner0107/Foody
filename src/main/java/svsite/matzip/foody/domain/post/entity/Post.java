package svsite.matzip.foody.domain.post.entity;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import svsite.matzip.foody.domain.auth.entity.User;
import svsite.matzip.foody.domain.favorite.entity.Favorite;
import svsite.matzip.foody.domain.image.entity.Image;
import svsite.matzip.foody.domain.post.api.dto.request.CreatePostDto;
import svsite.matzip.foody.domain.post.api.dto.request.UpdatePostDto;
import svsite.matzip.foody.global.entity.BaseEntity;

@Entity
@Table(name = "post")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(precision = 10, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 10, scale = 6)
  private BigDecimal longitude;

  @Enumerated(EnumType.STRING)
  private MarkerColor color;

  @Column(length = 255)
  private String address;
  @Column(nullable = false, length = 100)
  private String title;
  @Lob
  private String description;

  private LocalDateTime date;

  private Integer score;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Image> images = new ArrayList<>();

  @OneToMany(mappedBy = "post", orphanRemoval = true)
  private List<Favorite> favorites;

  public static Post create(CreatePostDto postDto, User user) {
    Post post = Post.builder()
        .latitude(postDto.latitude())
        .longitude(postDto.longitude())
        .color(postDto.color())
        .address(postDto.address())
        .title(postDto.title())
        .description(postDto.description())
        .date(postDto.date())
        .score(postDto.score())
        .user(user)
        .build();

    List<Image> images = postDto.imageUris()
        .stream()
        .map(uri -> Image.builder().uri(uri).build())
        .toList();

    post.addImages(images);
    return post;
  }

  public void update(UpdatePostDto postDto) {
    this.title = postDto.title();
    this.description = postDto.description();
    this.color = postDto.color();
    this.date = postDto.date();
    this.score = postDto.score();
  }

  public void updateImages(List<Image> updatedImages) {
    List<Image> imagesToAdd = findImagesToAdd(updatedImages);
    List<Image> imagesToRemove = findImagesToRemove(updatedImages);

    imagesToRemove.forEach(this::removeImage);
    imagesToAdd.forEach(this::addImage);
  }

  private List<Image> findImagesToAdd(List<Image> updatedImages) {
    return updatedImages.stream()
        .filter(newImage -> images.stream().noneMatch(existing -> existing.getUri().equals(newImage.getUri())))
        .toList();
  }

  private List<Image> findImagesToRemove(List<Image> updatedImages) {
    return images.stream()
        .filter(existing -> updatedImages.stream().noneMatch(newImage -> newImage.getUri().equals(existing.getUri())))
        .toList();
  }

  public void addImages(List<Image> images) {
    images.forEach(this::addImage);
  }

  public void addImage(Image image) {
    image.associateWithPost(this);
    this.images.add(image);
  }

  public void removeImage(Image image) {
    image.dissociateFromPost();
    images.remove(image);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if(!(o instanceof Post post)) return false;
    return id != null && id.equals(post.id);
  }
  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}

