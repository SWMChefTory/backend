package com.cheftory.api.recipe.batch;

import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipe.content.youtubemeta.client.VideoInfoClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

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
  @StepScope
  public StepExecutionListener marketContextListener(
      @Value("#{jobParameters['market']}") String marketParam) {
    return new StepExecutionListener() {
      private MarketContext.Scope scope;

      @Override
      public void beforeStep(StepExecution stepExecution) {
        Market market = Market.valueOf(marketParam);

        String countryCode =
            switch (market) {
              case KOREA -> "KR";
              case GLOBAL -> "US";
            };

        scope = MarketContext.with(new MarketContext.Info(market, countryCode));
      }

      @Override
      public ExitStatus afterStep(StepExecution stepExecution) {
        if (scope != null) scope.close();
        return stepExecution.getExitStatus();
      }
    };
  }

  @Bean
  public Step youtubeValidationStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      StepExecutionListener marketContextListener,
      RepositoryItemReader<RecipeYoutubeMeta> youtubeMetaReader,
      CompositeItemWriter<RecipeYoutubeMeta> compositeWriter) {
    return new StepBuilder("youtubeValidationStep", jobRepository)
        .<RecipeYoutubeMeta, RecipeYoutubeMeta>chunk(50, transactionManager)
        .listener(marketContextListener)
        .reader(youtubeMetaReader)
        .processor(youtubeUrlProcessor())
        .writer(compositeWriter)
        .build();
  }

  @Bean
  public RepositoryItemReader<RecipeYoutubeMeta> youtubeMetaReader() {
    return new RepositoryItemReaderBuilder<RecipeYoutubeMeta>()
        .name("youtubeMetaReader")
        .repository(youtubeMetaRepository)
        .methodName("findAll")
        .pageSize(50)
        .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  @StepScope
  public JdbcBatchItemWriter<RecipeYoutubeMeta> youtubeMetaJdbcWriter(
      @Value("#{jobParameters['market']}") String marketParam) {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .sql(
            """
          UPDATE recipe_youtube_meta
          SET status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP
          WHERE id = ? AND market = ?
          """)
        .dataSource(dataSource)
        .assertUpdates(false)
        .itemPreparedStatementSetter(
            (item, ps) -> {
              ps.setBytes(1, uuidToBytes(item.getId()));
              ps.setString(2, marketParam);
            })
        .build();
  }

  @Bean
  @StepScope
  public JdbcBatchItemWriter<RecipeYoutubeMeta> recipeStatusJdbcWriter(
      @Value("#{jobParameters['market']}") String marketParam) {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .sql(
            """
          UPDATE recipe
          SET recipe_status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP
          WHERE id = ? AND market = ?
          """)
        .dataSource(dataSource)
        .assertUpdates(false)
        .itemPreparedStatementSetter(
            (item, ps) -> {
              ps.setBytes(1, uuidToBytes(item.getRecipeId()));
              ps.setString(2, marketParam);
            })
        .build();
  }

  @Bean
  @StepScope
  public JdbcBatchItemWriter<RecipeYoutubeMeta> recipeHistoryJdbcWriter(
      @Value("#{jobParameters['market']}") String marketParam) {
    return new JdbcBatchItemWriterBuilder<RecipeYoutubeMeta>()
        .sql(
            """
          UPDATE recipe_history
          SET status = 'BLOCKED', updated_at = CURRENT_TIMESTAMP
          WHERE recipe_id = ? AND market = ?
          """)
        .dataSource(dataSource)
        .assertUpdates(false)
        .itemPreparedStatementSetter(
            (item, ps) -> {
              ps.setBytes(1, uuidToBytes(item.getRecipeId()));
              ps.setString(2, marketParam);
            })
        .build();
  }

  @Bean
  @StepScope
  public ItemWriter<RecipeYoutubeMeta> recipeCreateRefundWriter(
      RecipeCreditPort recipeCreditPort,
      JdbcTemplate jdbcTemplate,
      @Value("#{jobParameters['market']}") String marketParam) {
    return items -> {
      for (RecipeYoutubeMeta item : items) {
        if (item == null || item.getStatus() != YoutubeMetaStatus.BLOCKED) continue;

        UUID recipeId = item.getRecipeId();

        var row =
            jdbcTemplate.queryForObject(
                """
                SELECT user_id, credit_cost
                FROM recipe
                WHERE id = ? AND market = ?
                """,
                (rs, rowNum) ->
                    new Object[] {bytesToUuid(rs.getBytes("user_id")), rs.getLong("credit_cost")},
                uuidToBytes(recipeId),
                marketParam);

        if (row == null) continue;

        UUID userId = (UUID) row[0];
        long creditCost = (long) row[1];

        recipeCreditPort.refundRecipeCreate(userId, recipeId, creditCost);
      }
    };
  }

  private UUID bytesToUuid(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    long high = bb.getLong();
    long low = bb.getLong();
    return new UUID(high, low);
  }

  private byte[] uuidToBytes(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  @Bean
  @StepScope
  public CompositeItemWriter<RecipeYoutubeMeta> compositeWriter(
      JdbcBatchItemWriter<RecipeYoutubeMeta> youtubeMetaJdbcWriter,
      JdbcBatchItemWriter<RecipeYoutubeMeta> recipeStatusJdbcWriter,
      JdbcBatchItemWriter<RecipeYoutubeMeta> recipeHistoryJdbcWriter,
      ItemWriter<RecipeYoutubeMeta> recipeCreateRefundWriter) {
    var writer = new CompositeItemWriter<RecipeYoutubeMeta>();
    writer.setDelegates(
        List.of(
            youtubeMetaJdbcWriter,
            recipeStatusJdbcWriter,
            recipeHistoryJdbcWriter,
            recipeCreateRefundWriter));
    return writer;
  }

  @Bean
  public ItemProcessor<RecipeYoutubeMeta, RecipeYoutubeMeta> youtubeUrlProcessor() {
    return item -> {
      if (item.getStatus() != YoutubeMetaStatus.ACTIVE) {
        return null;
      }

      boolean isValid = checkYoutubeUrlWithApi(item);
      if (!isValid) {
        log.warn("Invalid video - ID: {}, URI: {}", item.getVideoId(), item.getVideoUri());
        item.block();
        return item;
      }
      return null;
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
