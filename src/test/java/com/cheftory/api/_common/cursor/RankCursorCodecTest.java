package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankCursorCodec")
class RankCursorCodecTest {

    private final RankCursorCodec codec = new RankCursorCodec();

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() throws CursorException {
        RankCursor cursor = new RankCursor("ranking:key", 10);

        String encoded = codec.encode(cursor);
        RankCursor decoded = codec.decode(encoded);

        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    @DisplayName("should throw CursorException when cursor is invalid")
    void shouldThrowWhenCursorInvalid() {
        assertThatThrownBy(() -> codec.decode("invalid-cursor"))
                .isInstanceOf(CursorException.class)
                .extracting("error")
                .isEqualTo(CursorErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("should throw CursorException when cursor is null")
    void shouldThrowWhenCursorNull() {
        assertThatThrownBy(() -> codec.decode(null))
                .isInstanceOf(CursorException.class)
                .extracting("error")
                .isEqualTo(CursorErrorCode.INVALID_CURSOR);
    }
}
