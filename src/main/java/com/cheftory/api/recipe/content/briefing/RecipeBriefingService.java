package com.cheftory.api.recipe.content.briefing;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.briefing.client.BriefingClient;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeBriefingService {

    private final BriefingClient briefingClient;
    private final RecipeBriefingRepository recipeBriefingRepository;
    private final Clock clock;

    @DbThrottled
    public void create(String videoId, UUID recipeId) {
        BriefingClientResponse response = briefingClient.fetchBriefing(videoId);
        List<RecipeBriefing> recipeBriefings = response.toRecipeBriefing(recipeId, clock);
        recipeBriefingRepository.saveAll(recipeBriefings);
    }

    public List<RecipeBriefing> gets(UUID recipeId) {
        return recipeBriefingRepository.findAllByRecipeId(recipeId);
    }
}
