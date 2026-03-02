package com.cheftory.api.tracking;

import com.cheftory.api._common.Clock;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.entity.RecipeClick;
import com.cheftory.api.tracking.entity.RecipeImpression;
import com.cheftory.api.tracking.repository.RecipeClickJpaRepository;
import com.cheftory.api.tracking.repository.RecipeImpressionJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 레시피 추적 서비스.
 *
 * <p>프론트엔드에서 수집한 노출/클릭 데이터를 저장합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final RecipeImpressionJpaRepository impressionRepository;
    private final RecipeClickJpaRepository clickRepository;
    private final Clock clock;

    /**
     * 레시피 노출 배치 저장.
     *
     * @param userId 사용자 ID
     * @param request 노출 배치 요청
     */
    @Transactional
    public void saveImpressions(UUID userId, TrackingImpressionRequest request) {
        List<RecipeImpression> entities = request.impressions().stream()
                .map(item -> RecipeImpression.create(
                        clock,
                        userId,
                        request.requestId(),
                        request.surfaceType(),
                        item.recipeId(),
                        item.position(),
                        item.timestamp()))
                .toList();
        impressionRepository.saveAll(entities);
    }

    /**
     * 레시피 클릭 단건 저장.
     *
     * @param userId 사용자 ID
     * @param request 클릭 요청
     */
    public void saveClick(UUID userId, TrackingClickRequest request) {
        RecipeClick entity = RecipeClick.create(
                clock,
                userId,
                request.requestId(),
                request.surfaceType(),
                request.recipeId(),
                request.position(),
                request.timestamp());
        clickRepository.save(entity);
    }
}
