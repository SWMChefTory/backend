package com.cheftory.api._support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * RedisTemplate가 필요한 Redis 통합 테스트 공통 지원 클래스.
 */
public abstract class RedisTemplateTestSupport extends TestKeyNamespaceSupport {

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;
}
