package com.cheftory.api.auth.verifier.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GoogleTokenHttpApi {

    @GetExchange("/tokeninfo")
    String fetchTokenInfo(@RequestParam("id_token") String idToken);
}
