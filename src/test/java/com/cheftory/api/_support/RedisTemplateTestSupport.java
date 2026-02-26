package com.cheftory.api._support;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * RedisTemplate가 필요한 Redis 통합 테스트 공통 지원 클래스.
 */
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RedisTemplateTestSupport extends TestKeyNamespaceSupport {

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;
}
