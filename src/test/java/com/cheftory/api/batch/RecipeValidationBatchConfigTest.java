package com.cheftory.api.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.history.RecipeHistoryRepository;
import com.cheftory.api.recipeinfo.history.RecipeHistoryStatus;
import com.cheftory.api.recipeinfo.recipe.RecipeRepository;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaStatus;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import com.cheftory.api.recipeinfo.youtubemeta.client.VideoInfoClient;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@SpringBatchTest
class RecipeValidationBatchConfigTest {

  @Autowired private JobLauncher jobLauncher;
  @Autowired private Job youtubeValidationJob;
  @Autowired private RecipeYoutubeMetaRepository youtubeMetaRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private RecipeHistoryRepository recipeHistoryRepository;
  @Autowired private com.cheftory.api._common.Clock clock;

  @MockitoBean
  private VideoInfoClient videoInfoClient;

  @BeforeEach
  void setUp() {
    recipeHistoryRepository.deleteAll();
    youtubeMetaRepository.deleteAll();
    recipeRepository.deleteAll();
  }

  @Test
  @DisplayName("유효하지 않은 YouTube 비디오를 BLOCKED 상태로 변경")
  void shouldBlockInvalidYoutubeVideos() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID invalidRecipeId = createRecipe();
    UUID invalidYoutubeMetaId =
        createYoutubeMeta(
            invalidRecipeId, "invalid_video_id1", "https://www.youtube.com/watch?v=invalid_video_id1");
    UUID invalidHistoryId = createRecipeHistory(userId, invalidRecipeId);

    when(videoInfoClient.isBlockedVideo(any())).thenReturn(true);

    JobExecution jobExecution = runBatchJob();

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertYoutubeMetaStatus(invalidYoutubeMetaId, YoutubeMetaStatus.BLOCKED);
    assertRecipeStatus(invalidRecipeId, RecipeStatus.BLOCKED);
    assertHistoryStatus(invalidHistoryId, RecipeHistoryStatus.BLOCKED);
  }

  @Test
  @DisplayName("유효한 YouTube 비디오는 ACTIVE 상태 유지")
  void shouldKeepValidYoutubeVideosActive() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID validRecipeId = createRecipe();
    UUID validYoutubeMetaId =
        createYoutubeMeta(
            validRecipeId, "valid_video_id2", "https://www.youtube.com/watch?v=valid_video_id2");
    UUID validHistoryId = createRecipeHistory(userId, validRecipeId);

    when(videoInfoClient.isBlockedVideo(any())).thenReturn(false);

    JobExecution jobExecution = runBatchJob();

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertYoutubeMetaStatus(validYoutubeMetaId, YoutubeMetaStatus.ACTIVE);
    assertRecipeStatus(validRecipeId, RecipeStatus.IN_PROGRESS);
    assertHistoryStatus(validHistoryId, RecipeHistoryStatus.ACTIVE);
  }

  @Test
  @DisplayName("YouTube API 호출 실패 시 비디오를 BLOCKED 처리")
  void shouldBlockVideosWhenApiCallFails() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID recipeId = createRecipe();
    UUID youtubeMetaId =
        createYoutubeMeta(
            recipeId, "test_video_id4", "https://www.youtube.com/watch?v=test_video_id4");
    createRecipeHistory(userId, recipeId);

    when(videoInfoClient.isBlockedVideo(any())).thenThrow(new RuntimeException("API Error"));

    JobExecution jobExecution = runBatchJob();

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertYoutubeMetaStatus(youtubeMetaId, YoutubeMetaStatus.BLOCKED);
  }

  @Test
  @DisplayName("ACTIVE 상태의 비디오만 검증 대상")
  void shouldProcessOnlyActiveVideos() throws Exception {
    UUID activeRecipeId = createRecipe();
    UUID activeMetaId =
        createYoutubeMeta(
            activeRecipeId, "active_video", "https://www.youtube.com/watch?v=active_video");

    UUID blockedRecipeId = createRecipe();
    UUID blockedMetaId =
        createBlockedYoutubeMeta(
            blockedRecipeId, "blocked_video", "https://www.youtube.com/watch?v=blocked_video");

    when(videoInfoClient.isBlockedVideo(any())).thenReturn(false);

    JobExecution jobExecution = runBatchJob();

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertYoutubeMetaStatus(activeMetaId, YoutubeMetaStatus.ACTIVE);
    assertYoutubeMetaStatus(blockedMetaId, YoutubeMetaStatus.BLOCKED);
  }

  private JobExecution runBatchJob() throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("testId", UUID.randomUUID().toString())
            .toJobParameters();
    return jobLauncher.run(youtubeValidationJob, jobParameters);
  }

  private void assertYoutubeMetaStatus(UUID metaId, YoutubeMetaStatus expectedStatus) {
    var meta = youtubeMetaRepository.findById(metaId).orElseThrow();
    assertThat(meta.getStatus()).isEqualTo(expectedStatus);
  }

  private void assertRecipeStatus(UUID recipeId, RecipeStatus expectedStatus) {
    var recipe = recipeRepository.findById(recipeId).orElseThrow();
    assertThat(recipe.getRecipeStatus()).isEqualTo(expectedStatus);
  }

  private void assertHistoryStatus(UUID historyId, RecipeHistoryStatus expectedStatus) {
    var history = recipeHistoryRepository.findById(historyId).orElseThrow();
    assertThat(history.getStatus()).isEqualTo(expectedStatus);
  }

  private UUID createRecipe() {
    Recipe recipe = Recipe.create(clock);
    return recipeRepository.save(recipe).getId();
  }

  private UUID createYoutubeMeta(UUID recipeId, String videoId, String videoUrl) {
    YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
    doReturn(URI.create(videoUrl)).when(videoInfo).getVideoUri();
    doReturn(videoId).when(videoInfo).getVideoId();
    doReturn("Test Video Title").when(videoInfo).getTitle();
    doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
        .when(videoInfo)
        .getThumbnailUrl();
    doReturn(180).when(videoInfo).getVideoSeconds();

    RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
    return youtubeMetaRepository.save(youtubeMeta).getId();
  }

  private UUID createBlockedYoutubeMeta(UUID recipeId, String videoId, String videoUrl) {
    YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
    doReturn(URI.create(videoUrl)).when(videoInfo).getVideoUri();
    doReturn(videoId).when(videoInfo).getVideoId();
    doReturn("Blocked Video Title").when(videoInfo).getTitle();
    doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
        .when(videoInfo)
        .getThumbnailUrl();
    doReturn(180).when(videoInfo).getVideoSeconds();

    RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
    youtubeMeta.block();
    return youtubeMetaRepository.save(youtubeMeta).getId();
  }

  private UUID createRecipeHistory(UUID userId, UUID recipeId) {
    RecipeHistory history = RecipeHistory.create(clock, userId, recipeId);
    return recipeHistoryRepository.save(history).getId();
  }
}

