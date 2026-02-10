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

/**
 * 레시피 유튜브 메타 엔티티
 *
 * <p>레시피와 연결된 유튜브 비디오의 메타 정보(제목, 채널명, 썸네일 등)를 저장하는 엔티티입니다.</p>
 */
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeYoutubeMeta extends MarketScope {
    @Id
    private UUID id;

    /**
     * 비디오 URI
     */
    @Column(nullable = false)
    private URI videoUri;

    /**
     * 비디오 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 채널명
     */
    @Column(nullable = false)
    private String channelTitle;

    /**
     * 썸네일 URL
     */
    @Column(nullable = false)
    private URI thumbnailUrl;

    /**
     * 비디오 길이 (초)
     */
    @Column(nullable = false)
    private Integer videoSeconds;

    /**
     * 생성 일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 메타 정보 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private YoutubeMetaStatus status;

    /**
     * 수정 일시
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 연결된 레시피 ID
     */
    @Column(nullable = false)
    private UUID recipeId;

    /**
     * 비디오 타입 (일반/쇼츠)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private YoutubeMetaType type;

    /**
     * 유튜브 메타 엔티티 생성
     *
     * @param youtubeVideoInfo 유튜브 비디오 정보
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 생성된 유튜브 메타 엔티티
     */
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

    /**
     * 비디오 ID 추출
     *
     * @return 비디오 ID
     */
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

    public void failed() {
        this.status = YoutubeMetaStatus.FAILED;
    }
}
