package com.cheftory.api.ranking.interaction;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingImpressionRepository extends JpaRepository<RankingImpression, UUID> {}
