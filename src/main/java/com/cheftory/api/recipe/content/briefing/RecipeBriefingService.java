package com.cheftory.api.recipe.content.briefing;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.client.BriefingClient;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import com.cheftory.api.recipe.content.briefing.respotiory.RecipeBriefingRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 브리핑 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeBriefingService {

    private final BriefingClient client;
    private final RecipeBriefingRepository repository;
    private final Clock clock;

    /**
     * 레시피 브리핑 생성
     *
     * <p>외부 클라이언트를 통해 비디오 ID에 해당하는 브리핑 정보를 가져와 저장합니다.</p>
     *
     * @param videoId 유튜브 비디오 ID
     * @param recipeId 레시피 ID
     * @throws RecipeBriefingException 브리핑 생성 중 예외 발생 시
     */
    public void create(String videoId, UUID recipeId) throws RecipeBriefingException {
        BriefingClientResponse response = client.fetchBriefing(videoId);
        List<RecipeBriefing> recipeBriefings = response.toRecipeBriefing(recipeId, clock);
        repository.saveAll(recipeBriefings);
    }

    /**
     * 레시피 ID로 브리핑 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 브리핑 목록
     */
    public List<RecipeBriefing> gets(UUID recipeId) {
        return repository.findAllByRecipeId(recipeId);
    }
}
