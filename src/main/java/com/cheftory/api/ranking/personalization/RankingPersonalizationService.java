package com.cheftory.api.ranking.personalization;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 랭킹 개인화 서비스.
 *
 * <p>사용자의 시드 아이템을 기반으로 개인화 프로필을 생성합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RankingPersonalizationService {

    private static final int KEYWORDS_TOP_N = 20;
    private static final int CHANNELS_TOP_N = 10;

    private final RankingPersonalizationSearchPort rankingPersonalizationSearchPort;

    /**
     * 시드 아이템들을 기반으로 개인화 프로필을 집계합니다.
     *
     * @param seedIds 시드 아이템 ID 목록
     * @return 개인화 프로필
     * @throws RankingPersonalizationException 처리 예외
     */
    public PersonalizationProfile aggregateProfile(List<UUID> seedIds) throws RankingPersonalizationException {
        List<String> ids = seedIds.stream().map(UUID::toString).toList();
        List<RankingPersonalizationSeed> seedDocs = rankingPersonalizationSearchPort.mgetSeeds(ids);

        Function<String, Integer> one = v -> 1;

        Map<String, Integer> keywordCounts = seedDocs.stream()
                .flatMap(seedDoc -> requireNonNull(seedDoc.keywords(), "seedDoc.keywords is null").stream())
                .collect(Collectors.toMap(Function.identity(), one, Integer::sum));

        Map<String, Integer> channelCounts = seedDocs.stream()
                .map(seedDoc -> requireNonNull(seedDoc.channelTitle(), "seedDoc.channelTitle is null"))
                .collect(Collectors.toMap(Function.identity(), one, Integer::sum));

        return new PersonalizationProfile(topK(keywordCounts, KEYWORDS_TOP_N), topK(channelCounts, CHANNELS_TOP_N));
    }

    /**
     * 상위 K개 항목을 추출합니다.
     *
     * @param counts 항목별 카운트 맵
     * @param k 추출할 개수
     * @return 상위 K개 항목
     */
    private static List<String> topK(Map<String, Integer> counts, int k) {
        return counts.entrySet().stream()
                .sorted((a, b) -> {
                    int byCount = Integer.compare(b.getValue(), a.getValue());
                    return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
                })
                .limit(k)
                .map(Map.Entry::getKey)
                .toList();
    }
}
