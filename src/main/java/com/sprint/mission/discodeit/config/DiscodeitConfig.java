package com.sprint.mission.discodeit.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@Configuration
//@EnableConfigurationProperties(DiscodeitRepoConfig.class)
public class DiscodeitConfig {

}

