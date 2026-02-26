package com.cheftory.api.recipe.content.briefing.client;

import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientRequest;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BriefingHttpApi {

    @PostExchange("/briefings")
    BriefingClientResponse fetchBriefing(@RequestBody BriefingClientRequest request);
}
