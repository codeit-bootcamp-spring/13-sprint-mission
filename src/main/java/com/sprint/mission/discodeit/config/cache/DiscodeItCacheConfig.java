package com.sprint.mission.discodeit.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@Slf4j
public class DiscodeItCacheConfig {
    /*
    캐시 드랍시의 이유, 혹은 상세 정보를 확인하기 위해 설정을 자바 객체로 치환.
     */

    // 캐시 해야 할 정보 종류
    // 1. 채널 2. 알림 3. 사용자


    static public final String CHANNEL_CACHE = "channel";
    static public final String NOTIFICATION_CACHE = "notification";
    static public final String USER_CACHE = "user";

    // @PostCinstructor 이후 한번 실행되는 ㅡㄹ래스 = ApplicationRunner
    // 현재 시스템의 캐시 매니저와 캐시 정보를 확인할 수 있음.
    @Bean
    public ApplicationRunner cacheStartupReport(CacheManager cacheManager) {
        return args -> {
            log.info("[Cache] CacheManager = {}", cacheManager.getClass().getSimpleName());
            // 각 캐스 스토리지마다 스토리지의 이름을 로그로 반환.
            cacheManager.getCacheNames().forEach(name ->
                    log.info("[Cache]   {} → {}", name,
                            cacheManager.getCache(name).getNativeCache().getClass().getSimpleName()));
        };
    }
}
