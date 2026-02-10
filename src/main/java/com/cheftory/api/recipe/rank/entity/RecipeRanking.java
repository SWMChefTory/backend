package com.cheftory.api.recipe.rank.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.recipe.rank.RankingType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 랭킹 도메인 엔티티
 *
 * <p>특정 시점의 랭킹 정보를 정의하고 관리하는 도메인 객체입니다.
 * 랭킹의 식별자(Key), 타입, 유효 기간(TTL) 등의 정책을 캡슐화합니다.</p>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeRanking {

    /**
     * 랭킹 타입
     */
    private final RankingType type;

    /**
     * 랭킹 식별자 (Redis Key)
     */
    private final String key;

    /**
     * 랭킹 유효 기간
     */
    private final Duration ttl;

    private static final Duration DEFAULT_TTL = Duration.ofDays(2);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DELIMITER = ":";

    /**
     * 랭킹 도메인 객체 생성
     *
     * @param type 랭킹 타입
     * @param clock 시계 객체
     * @return 생성된 랭킹 객체
     */
    public static RecipeRanking create(RankingType type, Clock clock) {
        String key = generateKey(type, clock.now());
        return new RecipeRanking(type, key, DEFAULT_TTL);
    }

    /**
     * 최신 랭킹을 가리키는 포인터 키 조회
     *
     * @return 포인터 키
     */
    public String getLatestPointerKey() {
        return buildKey(getTypePrefix(this.type), "latest");
    }

    /**
     * 최신 랭킹 포인터 키 생성 (정적 메서드)
     *
     * <p>조회 시 사용됩니다.</p>
     *
     * @param type 랭킹 타입
     * @return 포인터 키
     */
    public static String getLatestPointerKey(RankingType type) {
        return buildKey(getTypePrefix(type), "latest");
    }

    private static String generateKey(RankingType type, LocalDateTime now) {
        return buildKey(getTypePrefix(type), "ranking", now.format(FORMATTER));
    }

    private static String buildKey(String... parts) {
        Market market = MarketContext.required().market();
        return market.name().toLowerCase() + DELIMITER + String.join(DELIMITER, parts);
    }

    private static String getTypePrefix(RankingType type) {
        return switch (type) {
            case TRENDING -> "trendRecipe";
            case CHEF -> "chefRecipe";
        };
    }
}
