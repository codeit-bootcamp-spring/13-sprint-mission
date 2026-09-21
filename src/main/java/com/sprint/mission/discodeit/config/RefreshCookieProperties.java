package com.sprint.mission.discodeit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discodeit.security.refresh-cookie")
public record RefreshCookieProperties(
        boolean httpOnly,
        boolean secure,
        String sameSite
) {
}
