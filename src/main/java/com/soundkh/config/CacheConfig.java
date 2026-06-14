package com.soundkh.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@EnableAsync
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        var json = new GenericJackson2JsonRedisSerializer();
        var valueSerializer = RedisSerializationContext.SerializationPair.fromSerializer(json);

        var defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(valueSerializer)
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(Map.of(
                        "tracks", defaults.entryTtl(Duration.ofMinutes(5)),
                        "channels", defaults.entryTtl(Duration.ofMinutes(10)),
                        "feed", defaults.entryTtl(Duration.ofMinutes(2))
                ))
                .build();
    }
}
