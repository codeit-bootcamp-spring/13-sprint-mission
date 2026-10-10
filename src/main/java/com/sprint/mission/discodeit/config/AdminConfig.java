package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminConfig {
}
