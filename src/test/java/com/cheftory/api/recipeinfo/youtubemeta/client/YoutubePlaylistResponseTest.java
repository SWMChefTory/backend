package com.cheftory.api.recipeinfo.youtubemeta.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("YoutubePlaylistResponse")
class YoutubePlaylistResponseTest {

  @Test
  @DisplayName("hasItems()는 items가 있으면 true를 반환한다")
  void hasItemsReturnsTrueWhenItemsExist() {
    YoutubePlaylistResponse.Snippet snippet = new YoutubePlaylistResponse.Snippet("videoId123");
    YoutubePlaylistResponse.Item item = new YoutubePlaylistResponse.Item(snippet);
    YoutubePlaylistResponse response = new YoutubePlaylistResponse(List.of(item));

    boolean result = response.hasItems();

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("hasItems()는 items가 비어있으면 false를 반환한다")
  void hasItemsReturnsFalseWhenItemsEmpty() {
    YoutubePlaylistResponse response = new YoutubePlaylistResponse(List.of());

    boolean result = response.hasItems();

    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("hasItems()는 items가 null이면 false를 반환한다")
  void hasItemsReturnsFalseWhenItemsNull() {
    YoutubePlaylistResponse response = new YoutubePlaylistResponse(null);

    boolean result = response.hasItems();

    assertThat(result).isFalse();
  }
}
