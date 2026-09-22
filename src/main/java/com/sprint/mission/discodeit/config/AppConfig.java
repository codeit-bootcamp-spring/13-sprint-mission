package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@EnableScheduling
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
