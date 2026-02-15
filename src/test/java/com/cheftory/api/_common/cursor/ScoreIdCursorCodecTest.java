package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScoreIdCursorCodec")
class ScoreIdCursorCodecTest {

    private final ScoreIdCursorCodec codec = new ScoreIdCursorCodec(mock(Clock.class));

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() throws CursorException {
        ScoreIdCursor cursor = new ScoreIdCursor(1.25, "id-1", "2026-02-13T10:00:00", "pit-1");

        String encoded = codec.encode(cursor);
        ScoreIdCursor decoded = codec.decode(encoded);

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
    @DisplayName("should throw CursorException when cursor is blank")
    void shouldThrowWhenCursorBlank() {
        assertThatThrownBy(() -> codec.decode(" "))
                .isInstanceOf(CursorException.class)
                .extracting("error")
                .isEqualTo(CursorErrorCode.INVALID_CURSOR);
    }
}
