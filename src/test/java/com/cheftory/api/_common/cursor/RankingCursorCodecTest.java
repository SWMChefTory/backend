package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingCursorCodec")
class RankingCursorCodecTest {

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() throws CursorException {
        RankingCursorCodec codec = new RankingCursorCodec();
        RankingCursor cursor = new RankingCursor(UUID.randomUUID(), "after");

        String encoded = codec.encode(cursor);
        RankingCursor decoded = codec.decode(encoded);

        assertThat(decoded.requestId()).isEqualTo(cursor.requestId());
        assertThat(decoded.searchAfter()).isEqualTo(cursor.searchAfter());
    }

    @Test
    @DisplayName("should throw CursorException when cursor is invalid")
    void shouldThrowWhenCursorInvalid() {
        RankingCursorCodec codec = new RankingCursorCodec();

        assertThatThrownBy(() -> codec.decode("invalid-cursor"))
                .isInstanceOf(CursorException.class)
                .extracting("error")
                .isEqualTo(CursorErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("should throw CursorException when cursor is blank")
    void shouldThrowWhenCursorBlank() {
        RankingCursorCodec codec = new RankingCursorCodec();

        assertThatThrownBy(() -> codec.decode(" "))
                .isInstanceOf(CursorException.class)
                .extracting("error")
                .isEqualTo(CursorErrorCode.INVALID_CURSOR);
    }
}
