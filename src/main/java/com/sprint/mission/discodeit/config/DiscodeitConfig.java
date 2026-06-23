package com.sprint.mission.discodeit.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(DiscodeitRepoConfig.class)
public class DiscodeitConfig {

    private final DiscodeitRepoConfig discodeitRepoConfig;

    public DiscodeitConfig(DiscodeitRepoConfig discodeitRepoConfig) {
        this.discodeitRepoConfig = discodeitRepoConfig;
    }

    public Path getFilePath(){
        return Paths.get(this.discodeitRepoConfig.getPath());
    }

    public String getRepoType(){
        return this.discodeitRepoConfig.getType();
    }

}

