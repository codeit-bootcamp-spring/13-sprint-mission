package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discodeit.admin")
public record AdminProperties(
        String username,
        String email,
        String password
) {
}