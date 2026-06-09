package com.sprint.mission.discodeit;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(DiscodeitRepository.class)
public class DiscodeitConfig {

    private final DiscodeitRepository discodeitRepository;

    public DiscodeitConfig(DiscodeitRepository discodeitRepository) {
        this.discodeitRepository = discodeitRepository;
    }

    public Path getFilePath(){
        return Paths.get(this.discodeitRepository.getPath());
    }

    public String getRepoType(){
        return this.discodeitRepository.getType();
    }

}

