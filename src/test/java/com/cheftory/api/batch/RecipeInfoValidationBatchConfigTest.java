package com.cheftory.api.batch;

import static com.cheftory.api._common.region.Market.GLOBAL;
import static com.cheftory.api._common.region.Market.KOREA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.recipe.content.info.RecipeInfoRepository;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipe.content.youtubemeta.client.VideoInfoClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.history.RecipeHistoryRepository;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RecipeInfoValidationBatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job youtubeValidationJob;

    @Autowired
    private RecipeYoutubeMetaRepository youtubeMetaRepository;

    @Autowired
    private RecipeInfoRepository recipeInfoRepository;

    @Autowired
    private RecipeHistoryRepository recipeHistoryRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private VideoInfoClient videoInfoClient;

    @MockitoBean
    private RecipeCreditPort recipeCreditPort;

    @BeforeEach
    void setUp() {
        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            recipeHistoryRepository.deleteAll();
            youtubeMetaRepository.deleteAll();
            recipeInfoRepository.deleteAll();
        }

        try (var ignored = MarketContext.with(new MarketContext.Info(GLOBAL, "US"))) {
            recipeHistoryRepository.deleteAll();
            youtubeMetaRepository.deleteAll();
            recipeInfoRepository.deleteAll();
        }
    }

    @Test
    @DisplayName("유효하지 않은 YouTube 비디오를 BLOCKED 상태로 변경 + 환불 호출")
    void shouldBlockInvalidYoutubeVideos() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        UUID invalidRecipeId;
        UUID invalidYoutubeMetaId;
        UUID invalidHistoryId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            invalidRecipeId = createRecipe(creditCost, KOREA.name());
            invalidYoutubeMetaId = createYoutubeMeta(
                    invalidRecipeId, "invalid_video_id1", "https://www.youtube.com/watch?v=invalid_video_id1");
            invalidHistoryId = createRecipeHistory(userId, invalidRecipeId);
        }

        when(videoInfoClient.isBlockedVideo(any())).thenReturn(true);

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(invalidYoutubeMetaId, YoutubeMetaStatus.BLOCKED);
            assertRecipeStatus(invalidRecipeId, RecipeStatus.BLOCKED);
            assertHistoryStatus(invalidHistoryId, RecipeHistoryStatus.BLOCKED);
        }

        verify(recipeCreditPort, times(1)).refundRecipeCreate(eq(userId), eq(invalidRecipeId), eq(creditCost));
    }

    @Test
    @DisplayName("유효한 YouTube 비디오는 ACTIVE 상태 유지 + 환불 호출 없음")
    void shouldKeepValidYoutubeVideosActive() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        UUID validRecipeId;
        UUID validYoutubeMetaId;
        UUID validHistoryId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            validRecipeId = createRecipe(creditCost, KOREA.name());
            validYoutubeMetaId = createYoutubeMeta(
                    validRecipeId, "valid_video_id2", "https://www.youtube.com/watch?v=valid_video_id2");
            validHistoryId = createRecipeHistory(userId, validRecipeId);
        }

        when(videoInfoClient.isBlockedVideo(any())).thenReturn(false);

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(validYoutubeMetaId, YoutubeMetaStatus.ACTIVE);
            assertRecipeStatus(validRecipeId, RecipeStatus.IN_PROGRESS);
            assertHistoryStatus(validHistoryId, RecipeHistoryStatus.ACTIVE);
        }

        verify(recipeCreditPort, never()).refundRecipeCreate(any(), any(), anyLong());
    }

    @Test
    @DisplayName("YouTube API 호출 실패 시 비디오를 BLOCKED 처리 + 환불 호출")
    void shouldBlockVideosWhenApiCallFails() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        UUID recipeId;
        UUID youtubeMetaId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            recipeId = createRecipe(creditCost, KOREA.name());
            youtubeMetaId =
                    createYoutubeMeta(recipeId, "test_video_id4", "https://www.youtube.com/watch?v=test_video_id4");
            createRecipeHistory(userId, recipeId);
        }

        when(videoInfoClient.isBlockedVideo(any())).thenThrow(new RuntimeException("API Error"));

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(youtubeMetaId, YoutubeMetaStatus.BLOCKED);
        }

        verify(recipeCreditPort, times(1)).refundRecipeCreate(eq(userId), eq(recipeId), eq(creditCost));
    }

    @Test
    @DisplayName("ACTIVE 상태의 비디오만 검증 대상(이미 BLOCKED면 환불/상태 변경 없음)")
    void shouldProcessOnlyActiveVideos() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        UUID activeMetaId;
        UUID blockedMetaId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            UUID activeRecipeId = createRecipe(creditCost, KOREA.name());
            activeMetaId =
                    createYoutubeMeta(activeRecipeId, "active_video", "https://www.youtube.com/watch?v=active_video");

            UUID blockedRecipeId = createRecipe(creditCost, KOREA.name());
            blockedMetaId = createBlockedYoutubeMeta(
                    blockedRecipeId, "blocked_video", "https://www.youtube.com/watch?v=blocked_video");
        }

        when(videoInfoClient.isBlockedVideo(any())).thenReturn(false);

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(activeMetaId, YoutubeMetaStatus.ACTIVE);
            assertYoutubeMetaStatus(blockedMetaId, YoutubeMetaStatus.BLOCKED);
        }

        verify(recipeCreditPort, never()).refundRecipeCreate(any(), any(), anyLong());
    }

    @Test
    @DisplayName("job parameter market에 따라 해당 마켓 데이터만 처리(환불도 해당 마켓만)")
    void shouldProcessOnlyTargetMarketByJobParam() throws Exception {
        UUID koreaUserId = UUID.randomUUID();
        long koreaCreditCost = 10L;

        UUID globalUserId = UUID.randomUUID();
        long globalCreditCost = 20L;

        UUID koreaRecipeId;
        UUID koreaMetaId;
        UUID koreaHistoryId;

        UUID globalRecipeId;
        UUID globalMetaId;
        UUID globalHistoryId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            koreaRecipeId = createRecipe(koreaCreditCost, KOREA.name());
            koreaMetaId =
                    createYoutubeMeta(koreaRecipeId, "korea_invalid", "https://www.youtube.com/watch?v=korea_invalid");
            koreaHistoryId = createRecipeHistory(koreaUserId, koreaRecipeId);
        }

        try (var ignored = MarketContext.with(new MarketContext.Info(GLOBAL, "US"))) {
            globalRecipeId = createRecipe(globalCreditCost, GLOBAL.name());
            globalMetaId = createYoutubeMeta(
                    globalRecipeId, "global_invalid", "https://www.youtube.com/watch?v=global_invalid");
            globalHistoryId = createRecipeHistory(globalUserId, globalRecipeId);
        }

        when(videoInfoClient.isBlockedVideo(any())).thenReturn(true);

        // KOREA만 실행
        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(koreaMetaId, YoutubeMetaStatus.BLOCKED);
            assertRecipeStatus(koreaRecipeId, RecipeStatus.BLOCKED);
            assertHistoryStatus(koreaHistoryId, RecipeHistoryStatus.BLOCKED);
        }

        try (var ignored = MarketContext.with(new MarketContext.Info(GLOBAL, "US"))) {
            assertYoutubeMetaStatus(globalMetaId, YoutubeMetaStatus.ACTIVE);
            assertRecipeStatus(globalRecipeId, RecipeStatus.IN_PROGRESS);
            assertHistoryStatus(globalHistoryId, RecipeHistoryStatus.ACTIVE);
        }

        verify(recipeCreditPort, times(1)).refundRecipeCreate(eq(koreaUserId), eq(koreaRecipeId), eq(koreaCreditCost));
        verify(recipeCreditPort, never())
                .refundRecipeCreate(eq(globalUserId), eq(globalRecipeId), eq(globalCreditCost));
    }

    @Test
    @DisplayName("YoutubeUri 파싱 실패 시 비디오를 BLOCKED 처리하고 API는 호출하지 않음 + 환불 호출")
    void shouldBlockWhenYoutubeUriParsingFails() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        UUID recipeId;
        UUID metaId;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            recipeId = createRecipe(creditCost, KOREA.name());
            metaId = createYoutubeMeta(recipeId, "no_v_param", "https://www.youtube.com/watch");
            createRecipeHistory(userId, recipeId);
        }

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        verify(videoInfoClient, never()).isBlockedVideo(any());

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            assertYoutubeMetaStatus(metaId, YoutubeMetaStatus.BLOCKED);
        }

        verify(recipeCreditPort, times(1)).refundRecipeCreate(eq(userId), eq(recipeId), eq(creditCost));
    }

    @Test
    @DisplayName("chunk/page 경계를 넘어도(51개) 모두 처리 + 환불 51회")
    void shouldProcessAcrossChunkBoundary() throws Exception {
        UUID userId = UUID.randomUUID();
        long creditCost = 10L;

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            for (int i = 0; i < 51; i++) {
                UUID recipeId = createRecipe(creditCost, KOREA.name());
                createYoutubeMeta(recipeId, "bulk_" + i, "https://www.youtube.com/watch?v=bulk_" + i);
                createRecipeHistory(userId, recipeId);
            }
        }

        when(videoInfoClient.isBlockedVideo(any())).thenReturn(true);

        JobExecution jobExecution = runBatchJob(KOREA.name());
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        try (var ignored = MarketContext.with(new MarketContext.Info(KOREA, "KR"))) {
            long blockedMeta = youtubeMetaRepository.findAll().stream()
                    .filter(m -> m.getStatus() == YoutubeMetaStatus.BLOCKED)
                    .count();
            long blockedRecipe = recipeInfoRepository.findAll().stream()
                    .filter(r -> r.getRecipeStatus() == RecipeStatus.BLOCKED)
                    .count();
            long blockedHistory = recipeHistoryRepository.findAll().stream()
                    .filter(h -> h.getStatus() == RecipeHistoryStatus.BLOCKED)
                    .count();

            assertThat(blockedMeta).isEqualTo(51);
            assertThat(blockedRecipe).isEqualTo(51);
            assertThat(blockedHistory).isEqualTo(51);
        }

        verify(recipeCreditPort, times(51)).refundRecipeCreate(eq(userId), any(), eq(creditCost));
    }

    private JobExecution runBatchJob(String market) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("testId", UUID.randomUUID().toString())
                .addString("market", market)
                .toJobParameters();
        return jobLauncher.run(youtubeValidationJob, jobParameters);
    }

    private void assertYoutubeMetaStatus(UUID metaId, YoutubeMetaStatus expectedStatus) {
        var meta = youtubeMetaRepository.findById(metaId).orElseThrow();
        assertThat(meta.getStatus()).isEqualTo(expectedStatus);
    }

    private void assertRecipeStatus(UUID recipeId, RecipeStatus expectedStatus) {
        var recipe = recipeInfoRepository.findById(recipeId).orElseThrow();
        assertThat(recipe.getRecipeStatus()).isEqualTo(expectedStatus);
    }

    private void assertHistoryStatus(UUID historyId, RecipeHistoryStatus expectedStatus) {
        var history = recipeHistoryRepository.findById(historyId).orElseThrow();
        assertThat(history.getStatus()).isEqualTo(expectedStatus);
    }

    /**
     * 테스트에서 RecipeInfo.create(clock)만 하면 user_id/credit_cost가 비어 있을 수 있어서 refund writer의 SELECT 결과에서
     * NPE가 날 수 있음.
     *
     * <p>그래서 저장 후 DB에 user_id/credit_cost를 직접 채워 넣음.
     */
    private UUID createRecipe(long creditCost, String market) {
        RecipeInfo recipeInfo = RecipeInfo.create(clock);
        UUID recipeId = recipeInfoRepository.save(recipeInfo).getId();

        jdbcTemplate.update(
                """
        UPDATE recipe
        SET credit_cost = ?
        WHERE id = ? AND market = ?
        """,
                creditCost,
                uuidToBytes(recipeId),
                market);

        return recipeId;
    }

    private UUID createYoutubeMeta(UUID recipeId, String videoId, String videoUrl) {
        YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
        doReturn(URI.create(videoUrl)).when(videoInfo).getVideoUri();
        doReturn(videoId).when(videoInfo).getVideoId();
        doReturn("Test Video Title").when(videoInfo).getTitle();
        doReturn("Test Channel").when(videoInfo).getChannelTitle();
        doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
                .when(videoInfo)
                .getThumbnailUrl();
        doReturn(180).when(videoInfo).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(videoInfo).getVideoType();

        RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
        return youtubeMetaRepository.save(youtubeMeta).getId();
    }

    private UUID createBlockedYoutubeMeta(UUID recipeId, String videoId, String videoUrl) {
        YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
        doReturn(URI.create(videoUrl)).when(videoInfo).getVideoUri();
        doReturn(videoId).when(videoInfo).getVideoId();
        doReturn("Blocked Video Title").when(videoInfo).getTitle();
        doReturn("Test Channel").when(videoInfo).getChannelTitle();
        doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
                .when(videoInfo)
                .getThumbnailUrl();
        doReturn(180).when(videoInfo).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(videoInfo).getVideoType();

        RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
        youtubeMeta.block();
        return youtubeMetaRepository.save(youtubeMeta).getId();
    }

    private UUID createRecipeHistory(UUID userId, UUID recipeId) {
        RecipeHistory history = RecipeHistory.create(clock, userId, recipeId);
        return recipeHistoryRepository.save(history).getId();
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
