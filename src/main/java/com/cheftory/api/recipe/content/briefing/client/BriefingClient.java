package com.cheftory.api.recipe.content.briefing.client;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;

public interface BriefingClient {
    BriefingClientResponse fetchBriefing(String videoId) throws RecipeBriefingException;
}
