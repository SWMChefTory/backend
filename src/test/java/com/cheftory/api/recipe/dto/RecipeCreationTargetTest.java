package com.cheftory.api.recipe.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationSource")
class RecipeCreationTargetTest {

    @Test
    @DisplayName("User record는 uri와 userId를 가진다")
    void userRecordHasUriAndUserId() {
        URI uri = URI.create("https://www.youtube.com/watch?v=user123");
        UUID userId = UUID.randomUUID();

        RecipeCreationTarget.User user = new RecipeCreationTarget.User(uri, userId);

        assertThat(user.uri()).isEqualTo(uri);
        assertThat(user.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Crawler record는 uri만 가진다")
    void crawlerRecordHasOnlyUri() {
        URI uri = URI.create("https://www.youtube.com/watch?v=crawler456");

        RecipeCreationTarget.Crawler crawler = new RecipeCreationTarget.Crawler(uri);

        assertThat(crawler.uri()).isEqualTo(uri);
    }

    @Test
    @DisplayName("User와 Crawler는 RecipeCreationSource 타입이다")
    void userAndCrawlerAreRecipeCreationSourceType() {
        URI uri = URI.create("https://www.youtube.com/watch?v=test");
        UUID userId = UUID.randomUUID();

        RecipeCreationTarget user = new RecipeCreationTarget.User(uri, userId);
        RecipeCreationTarget crawler = new RecipeCreationTarget.Crawler(uri);

        assertThat(user).isInstanceOf(RecipeCreationTarget.class);
        assertThat(crawler).isInstanceOf(RecipeCreationTarget.class);
    }

    @Test
    @DisplayName("동일한 데이터를 가진 User 인스턴스는 동일하다")
    void equalUserInstancesAreEqual() {
        URI uri = URI.create("https://www.youtube.com/watch?v=test");
        UUID userId = UUID.randomUUID();

        RecipeCreationTarget.User user1 = new RecipeCreationTarget.User(uri, userId);
        RecipeCreationTarget.User user2 = new RecipeCreationTarget.User(uri, userId);

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("동일한 데이터를 가진 Crawler 인스턴스는 동일하다")
    void equalCrawlerInstancesAreEqual() {
        URI uri = URI.create("https://www.youtube.com/watch?v=test");

        RecipeCreationTarget.Crawler crawler1 = new RecipeCreationTarget.Crawler(uri);
        RecipeCreationTarget.Crawler crawler2 = new RecipeCreationTarget.Crawler(uri);

        assertThat(crawler1).isEqualTo(crawler2);
    }
}
