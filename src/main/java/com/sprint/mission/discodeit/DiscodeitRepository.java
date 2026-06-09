package com.sprint.mission.discodeit;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "discodeit.repository")
public class DiscodeitRepository {
    private String type;
    private String path;
}
