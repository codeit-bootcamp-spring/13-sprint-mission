package com.sprint.mission.discodeit.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "discodeit.repository")
public class RepositoryProperties {

    private RepositoryType type = RepositoryType.JCF;
    private String FILE_DIRECTORY = ".discodeit";

}
