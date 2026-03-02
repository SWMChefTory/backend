package com.cheftory.api.recipe.content.youtubemeta.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 레시피 유튜브 메타 엔티티
 *
 * <p>레시피와 연결된 유튜브 비디오의 메타 정보(제목, 채널명, 썸네일 등)를 저장합니다.
 * 저장 컬럼은 URL 전체가 아니라 `videoId`를 사용하며, 필요 시 `getVideoUri()`로 watch URL을 계산합니다.</p>
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_recipe_youtube_meta_recipe_id", columnNames = "recipe_id")})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeYoutubeMeta extends MarketScope {
    @Id
    private UUID id;

    /**
     * 비디오 ID
     */
    @Column(nullable = false, length = 32)
    private String videoId;

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
     * 연결된 레시피 ID
     */
    @Column(nullable = false, unique = true)
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
                youtubeVideoInfo.getVideoId(),
                youtubeVideoInfo.getTitle(),
                youtubeVideoInfo.getChannelTitle(),
                youtubeVideoInfo.getThumbnailUrl(),
                youtubeVideoInfo.getVideoSeconds(),
                now,
                recipeId,
                youtubeVideoInfo.getVideoType());
    }

    /**
     * 저장된 videoId로 정규화된 watch URL을 동적으로 구성합니다.
     *
     * <p>DB에는 URI 대신 videoId만 저장하므로, 외부 응답/호환 용도로 계산값을 제공합니다.</p>
     */
    public URI getVideoUri() {
        return URI.create("https://www.youtube.com/watch?v=" + videoId);
    }
}
