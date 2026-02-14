package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ViewedAtCursorCodec")
class ViewedAtCursorCodecTest {

    private final ViewedAtCursorCodec codec = new ViewedAtCursorCodec();

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() throws CursorException {
        ViewedAtCursor cursor = new ViewedAtCursor(LocalDateTime.of(2026, 2, 13, 11, 0), UUID.randomUUID());

        String encoded = codec.encode(cursor);
        ViewedAtCursor decoded = codec.decode(encoded);

        assertThat(decoded).isEqualTo(cursor);
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
