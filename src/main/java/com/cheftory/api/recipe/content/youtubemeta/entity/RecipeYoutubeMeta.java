package com.cheftory.api.recipe.content.youtubemeta.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeYoutubeMeta extends MarketScope {
    @Id
    private UUID id;

    @Column(nullable = false)
    private URI videoUri;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String channelTitle;

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

    public static RecipeYoutubeMeta create(YoutubeVideoInfo youtubeVideoInfo, UUID recipeId, Clock clock) {

        LocalDateTime now = clock.now();

        return new RecipeYoutubeMeta(
                UUID.randomUUID(),
                youtubeVideoInfo.getVideoUri(),
                youtubeVideoInfo.getTitle(),
                youtubeVideoInfo.getChannelTitle(),
                youtubeVideoInfo.getThumbnailUrl(),
                youtubeVideoInfo.getVideoSeconds(),
                now,
                YoutubeMetaStatus.ACTIVE,
                now,
                recipeId,
                youtubeVideoInfo.getVideoType());
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
