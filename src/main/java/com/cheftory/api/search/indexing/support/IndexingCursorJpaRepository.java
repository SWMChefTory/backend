package com.cheftory.api.search.indexing.support;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexingCursorJpaRepository extends JpaRepository<IndexingCursor, String> {}
