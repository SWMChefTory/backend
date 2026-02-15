package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CountIdCursorCodec")
class CountIdCursorCodecTest {

    private final CountIdCursorCodec codec = new CountIdCursorCodec();

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() throws CursorException {
        CountIdCursor cursor = new CountIdCursor(100L, UUID.randomUUID());

        String encoded = codec.encode(cursor);
        CountIdCursor decoded = codec.decode(encoded);

        assertThat(decoded).isEqualTo(cursor);
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
