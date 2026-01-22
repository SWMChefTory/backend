package com.cheftory.api.ranking.personalization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PersonalizationProfile")
class PersonalizationProfileTest {

    @Test
    @DisplayName("should expose keywords and channels")
    void shouldExposeFields() {
        List<String> keywords = List.of("kimchi", "noodle");
        List<String> channels = List.of("channel-a");

        PersonalizationProfile profile = new PersonalizationProfile(keywords, channels);

        assertThat(profile.keywordsTop()).isEqualTo(keywords);
        assertThat(profile.channelsTop()).isEqualTo(channels);
    }
}
