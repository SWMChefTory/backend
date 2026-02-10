package com.cheftory.api.ranking.interaction;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 랭킹 이벤트 JPA 리포지토리.
 */
public interface RankingEventRepository extends JpaRepository<RankingEvent, UUID> {}
