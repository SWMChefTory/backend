package com.cheftory.api.recipeinfo.briefing;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.briefing.client.BriefingClient;
import com.cheftory.api.recipeinfo.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipeinfo.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipeinfo.briefing.exception.RecipeBriefingException;
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

  public void create(String videoId, UUID recipeId) {
    try {
      BriefingClientResponse response = briefingClient.fetchBriefing(videoId);
      List<RecipeBriefing> recipeBriefings = response.toRecipeBriefing(recipeId, clock);
      recipeBriefingRepository.saveAll(recipeBriefings);
    } catch (Exception e) {
      throw new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);
    }
  }

  public List<RecipeBriefing> finds(UUID recipeId) {
    return recipeBriefingRepository.findAllByRecipeId(recipeId);
  }
}
