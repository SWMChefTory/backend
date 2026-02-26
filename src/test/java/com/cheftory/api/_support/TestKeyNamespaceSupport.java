package com.cheftory.api._support;

import java.util.UUID;

/**
 * 테스트 간 Redis/캐시 키 충돌을 피하기 위한 네임스페이스 헬퍼.
 */
public abstract class TestKeyNamespaceSupport {

    private final String testRunPrefix = getClass().getSimpleName() + ":" + UUID.randomUUID();

    protected String key(String suffix) {
        return testRunPrefix + ":" + suffix;
    }
}
