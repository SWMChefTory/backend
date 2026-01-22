package com.cheftory.api.ranking.personalization;

import static java.util.Objects.requireNonNull;

import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingPersonalizationService {

    private static final int KEYWORDS_TOP_N = 20;
    private static final int CHANNELS_TOP_N = 10;

    private final RankingPersonalizationSearchPort rankingPersonalizationSearchPort;

    public PersonalizationProfile aggregateProfile(List<UUID> seedIds) {
        List<String> ids = seedIds.stream().map(UUID::toString).toList();
        List<SearchQuery> seedDocs = rankingPersonalizationSearchPort.mgetSearchQueries(ids);

        Function<String, Integer> one = v -> 1;

        Map<String, Integer> keywordCounts = seedDocs.stream()
                .flatMap(seedDoc -> requireNonNull(seedDoc.getKeywords(), "seedDoc.keywords is null").stream())
                .collect(Collectors.toMap(Function.identity(), one, Integer::sum));

        Map<String, Integer> channelCounts = seedDocs.stream()
                .map(seedDoc -> requireNonNull(seedDoc.getChannelTitle(), "seedDoc.channelTitle is null"))
                .collect(Collectors.toMap(Function.identity(), one, Integer::sum));

        return new PersonalizationProfile(topK(keywordCounts, KEYWORDS_TOP_N), topK(channelCounts, CHANNELS_TOP_N));
    }

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
