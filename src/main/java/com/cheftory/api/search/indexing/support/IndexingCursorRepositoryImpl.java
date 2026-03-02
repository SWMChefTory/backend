package com.cheftory.api.search.indexing.support;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.UpdatedAtIdCursor;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IndexingCursorRepositoryImpl implements IndexingCursorRepository {

    private final Clock clock;
    private final IndexingCursorJpaRepository jpaRepository;

    @Override
    public UpdatedAtIdCursor load(String pipelineName) {
        return jpaRepository
                .findById(pipelineName)
                .map(IndexingCursor::toCursor)
                .orElse(UpdatedAtIdCursor.initial());
    }

    @Override
    public void save(String pipelineName, UpdatedAtIdCursor cursor) {
        LocalDateTime now = clock.now();
        IndexingCursor entity = jpaRepository
                .findById(pipelineName)
                .orElseGet(() -> IndexingCursor.create(pipelineName, cursor, now));

        entity.updateCursor(cursor, now);
        jpaRepository.save(entity);
    }
}
