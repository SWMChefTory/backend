package com.cheftory.api.search.indexing.support;

import com.cheftory.api._common.cursor.UpdatedAtIdCursor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexingCursor {

    @Id
    @Column(nullable = false, length = 128)
    private String pipelineName;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(nullable = false, length = 64)
    private String lastId;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static IndexingCursor create(String pipelineName, UpdatedAtIdCursor cursor, LocalDateTime now) {
        return new IndexingCursor(
                pipelineName, cursor.lastUpdatedAt(), cursor.lastId().toString(), now);
    }

    public void updateCursor(UpdatedAtIdCursor cursor, LocalDateTime now) {
        this.lastUpdatedAt = cursor.lastUpdatedAt();
        this.lastId = cursor.lastId().toString();
        this.updatedAt = now;
    }

    public UpdatedAtIdCursor toCursor() {
        return new UpdatedAtIdCursor(lastUpdatedAt, UUID.fromString(lastId));
    }
}
