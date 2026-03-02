package com.cheftory.api.search.indexing.support;

import com.cheftory.api._common.cursor.UpdatedAtIdCursor;

public interface IndexingCursorRepository {

    UpdatedAtIdCursor load(String pipelineName);

    void save(String pipelineName, UpdatedAtIdCursor cursor);
}
