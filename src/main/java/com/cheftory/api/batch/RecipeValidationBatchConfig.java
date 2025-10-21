package com.cheftory.api.batch;

import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaStatus;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.client.VideoInfoClient;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RecipeValidationBatchConfig {

  private final DataSource dataSource;
  private final VideoInfoClient videoInfoClient;
  private final RecipeYoutubeMetaRepository youtubeMetaRepository;

  @Bean
  public Job youtubeValidationJob(JobRepository jobRepository, Step youtubeValidationStep) {
    return new JobBuilder("youtubeValidationJob", jobRepository)
        .start(youtubeValidationStep)
        .build();
  }

  @Bean
  public Step youtubeValidationStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {

    return new StepBuilder("youtubeValidationStep", jobRepository)
        .<RecipeYoutubeMeta, RecipeYoutubeMeta>chunk(50, transactionManager)
        .reader(youtubeMetaReader())
        .processor(youtubeUrlProcessor())
        .writer(compositeWriter())
        .build();
  }

  @Bean
  public RepositoryItemReader<RecipeYoutubeMeta> youtubeMetaReader() {
    return new RepositoryItemReaderBuilder<RecipeYoutubeMeta>()
        .name("youtubeMetaReader")
        .repository(youtubeMetaRepository)
        .methodName("findByStatus")
        .arguments(Collections.singletonList(YoutubeMetaStatus.ACTIVE))
        .pageSize(50)
        .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  public JdbcBatchItemWriter<RecipeYoutubeMeta> youtubeMetaJdbcWriter() {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .beanMapped()
        .sql("UPDATE recipe_youtube_meta SET status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP WHERE id = :id")
        .dataSource(dataSource)
        .build();
  }

  @Bean
  public JdbcBatchItemWriter<RecipeYoutubeMeta> recipeStatusJdbcWriter() {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .beanMapped()
        .sql("""
          UPDATE recipe
          SET recipe_status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP
          WHERE id = :recipeId
         """)
        .dataSource(dataSource)
        .build();
  }

  @Bean
  public JdbcBatchItemWriter<RecipeYoutubeMeta> recipeHistoryJdbcWriter() {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .beanMapped()
        .sql("""
          UPDATE recipe_history
            SET status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP
          WHERE recipe_id = :recipeId
         """)
        .dataSource(dataSource)
        .build();
  }


  @Bean
  public CompositeItemWriter<RecipeYoutubeMeta> compositeWriter() {
    var writer = new CompositeItemWriter<RecipeYoutubeMeta>();
    writer.setDelegates(java.util.List.of(
        youtubeMetaJdbcWriter(),
        recipeStatusJdbcWriter(),
        recipeHistoryJdbcWriter()
    ));
    return writer;
  }

  @Bean
  public ItemProcessor<RecipeYoutubeMeta, RecipeYoutubeMeta> youtubeUrlProcessor() {
    return item -> {
      try {
        boolean isValid = checkYoutubeUrlWithApi(item);
        if (!isValid) {
          log.warn("Invalid video - ID: {}, URI: {}", item.getVideoId(), item.getVideoUri());
          item.block();
          return item;
        }
        return null;
      } catch (Exception e) {
        log.error("Error processing video - ID: {}, URI: {}, Error: {}",
            item.getVideoId(), item.getVideoUri(), e.getMessage());
        item.block();
        return item;
      }
    };
  }

  private boolean checkYoutubeUrlWithApi(RecipeYoutubeMeta item) {
    try {
      YoutubeUri youtubeUri = YoutubeUri.from(item.getVideoUri());
      return !videoInfoClient.isBlockedVideo(youtubeUri);
    } catch (Exception e) {
      log.error("API 호출 실패 - Video ID: {}, Error: {}", item.getVideoId(), e.getMessage());
      return false;
    }
  }
}