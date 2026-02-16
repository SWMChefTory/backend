package com.cheftory.api._config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableRedisRepositories(basePackages = "com.cheftory.api")
@EnableCaching
public class RedisConfig {

    @Bean
    public GenericJacksonJsonRedisSerializer redisValueSerializer() {
        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.cheftory.api.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .build();

        return GenericJacksonJsonRedisSerializer.builder()
                .customize(b -> b.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS))
                .enableDefaultTyping(ptv)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, GenericJacksonJsonRedisSerializer serializer) {
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setHashKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(serializer);
        t.setHashValueSerializer(serializer);
        t.afterPropertiesSet();
        return t;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(GenericJacksonJsonRedisSerializer serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory, RedisCacheConfiguration redisCacheConfiguration) {
        RedisCacheConfiguration jwksCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration("apple-jwks", jwksCacheConfig)
                .build();
    }
}
