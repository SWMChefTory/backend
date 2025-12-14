package com.cheftory.api.recipeinfo.youtubemeta.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeYoutubeMeta extends MarketScope {
  @Id private UUID id;

  @Column(nullable = false)
  private URI videoUri;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private URI thumbnailUrl;

  @Column(nullable = false)
  private Integer videoSeconds;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private YoutubeMetaStatus status;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private UUID recipeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private YoutubeMetaType type;

  public static RecipeYoutubeMeta create(
      YoutubeVideoInfo youtubeVideoInfo, UUID recipeId, Clock clock) {
    return RecipeYoutubeMeta.builder()
        .id(UUID.randomUUID())
        .videoUri(youtubeVideoInfo.getVideoUri())
        .title(youtubeVideoInfo.getTitle())
        .thumbnailUrl(youtubeVideoInfo.getThumbnailUrl())
        .videoSeconds(youtubeVideoInfo.getVideoSeconds())
        .status(YoutubeMetaStatus.ACTIVE)
        .type(youtubeVideoInfo.getVideoType())
        .recipeId(recipeId)
        .createdAt(clock.now())
        .updatedAt(clock.now())
        .build();
  }

  public String getVideoId() {
    return UriComponentsBuilder.fromUri(videoUri).build().getQueryParams().getFirst("v");
  }

  public boolean isBanned() {
    return this.status == YoutubeMetaStatus.BANNED;
  }

  public void ban() {
    this.status = YoutubeMetaStatus.BANNED;
  }

  public void block() {
    this.status = YoutubeMetaStatus.BLOCKED;
  }

  public boolean isBlocked() {
    return this.status == YoutubeMetaStatus.BLOCKED;
  }
}
