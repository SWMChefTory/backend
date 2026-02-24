package com.cheftory.api.auth.verifier.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface AppleTokenHttpApi {

    @GetExchange("/auth/keys")
    String fetchJwks();
}
