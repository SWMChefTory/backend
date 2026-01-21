package com.cheftory.api.ranking.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingSnapshotService Tests")
class RankingSnapshotServiceTest {

    @Mock
    private RankingSnapshotKeyGenerator keyGenerator;

    @Mock
    private RankingSnapshotRepository snapshotRepository;

    @InjectMocks
    private RankingSnapshotService service;

    @Test
    @DisplayName("savePit saves PIT with ttl")
    void savePitSavesPitWithTtl() {
        UUID requestId = UUID.randomUUID();
        doReturn("pit:key").when(keyGenerator).pitKey(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        service.savePit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, "pit-1");

        verify(snapshotRepository).saveString("pit:key", "pit-1", Duration.ofMinutes(3));
    }

    @Test
    @DisplayName("getPit returns cached PIT")
    void getPitReturnsCachedPit() {
        UUID requestId = UUID.randomUUID();
        doReturn("pit:key").when(keyGenerator).pitKey(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        doReturn("pit-1").when(snapshotRepository).getString("pit:key");

        String result = service.getPit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        assertThat(result).isEqualTo("pit-1");
    }

    @Test
    @DisplayName("refreshPit updates ttl for pit and impression keys")
    void refreshPitUpdatesTtl() {
        UUID requestId = UUID.randomUUID();
        doReturn("pit:key").when(keyGenerator).pitKey(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);
        doReturn("pos:key").when(keyGenerator).impressionPosKey(requestId);

        service.refreshPit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        verify(snapshotRepository).expire("pit:key", Duration.ofMinutes(3));
        verify(snapshotRepository).expire("pos:key", Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("deletePit removes cached PIT")
    void deletePitRemovesCachedPit() {
        UUID requestId = UUID.randomUUID();
        doReturn("pit:key").when(keyGenerator).pitKey(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        service.deletePit(requestId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE);

        verify(snapshotRepository).delete("pit:key");
    }

    @Test
    @DisplayName("allocateImpressionPositions returns start position")
    void allocateImpressionPositionsReturnsStart() {
        UUID requestId = UUID.randomUUID();
        doReturn("pos:key").when(keyGenerator).impressionPosKey(requestId);
        doReturn(15L).when(snapshotRepository).incrementLong("pos:key", 5);

        long start = service.allocateImpressionPositions(requestId, 5);

        assertThat(start).isEqualTo(10L);
        verify(snapshotRepository).expire("pos:key", Duration.ofMinutes(3));
    }

    @Test
    @DisplayName("clearImpressionPositions deletes key")
    void clearImpressionPositionsDeletesKey() {
        UUID requestId = UUID.randomUUID();
        doReturn("pos:key").when(keyGenerator).impressionPosKey(requestId);

        service.clearImpressionPositions(requestId);

        verify(snapshotRepository).delete("pos:key");
    }
}
