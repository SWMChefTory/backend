package com.cheftory.api.account.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserId(@JsonProperty("user_id") String userId) {}
