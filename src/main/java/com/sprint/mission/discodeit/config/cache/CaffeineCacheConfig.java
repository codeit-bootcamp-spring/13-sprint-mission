package com.sprint.mission.discodeit.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@Slf4j
@ConditionalOnProperty(name = "discodeit.cache.type",havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeine(@Value("${discodeit.cache.max-size}") long maxSize,
                                             @Value("${discodeit.cache.ttl-seconds}") long ttlSeconds) {
        return Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .recordStats()  // actuator 관련 옵션.
                .removalListener((Object key, Object value, RemovalCause cause) -> {
                    log.debug("[Caffeine] - Cache remove - 키={} 사유={} (지정 삭제 여부={})", key, cause, cause.wasEvicted());
                });

    }
}
