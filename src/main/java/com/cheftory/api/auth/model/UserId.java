package com.cheftory.api.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserId(@JsonProperty("user_id") String userId) {}
