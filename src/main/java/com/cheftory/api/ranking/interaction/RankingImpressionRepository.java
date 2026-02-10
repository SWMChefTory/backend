package com.cheftory.api.ranking.interaction;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 랭킹 노출(impression) JPA 리포지토리.
 */
public interface RankingImpressionRepository extends JpaRepository<RankingImpression, UUID> {}
