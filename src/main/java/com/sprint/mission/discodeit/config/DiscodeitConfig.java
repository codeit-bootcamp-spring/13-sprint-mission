package com.sprint.mission.discodeit.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;


@EnableJpaAuditing
@Configuration
@EnableConfigurationProperties({
        JwtProperties.class
})
public class DiscodeitConfig {

    @Bean
    public Clock clock(){
        return Clock.systemDefaultZone();
    }

}

