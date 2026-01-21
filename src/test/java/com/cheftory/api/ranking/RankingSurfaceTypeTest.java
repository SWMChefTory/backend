package com.cheftory.api.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingSurfaceType")
class RankingSurfaceTypeTest {

    @Test
    @DisplayName("messageKey should use cuisine prefix")
    void messageKeyShouldUseCuisinePrefix() {
        assertThat(RankingSurfaceType.CUISINE_KOREAN.messageKey()).isEqualTo("recipe.cuisine.korean");
    }
}
