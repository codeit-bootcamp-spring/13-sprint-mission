package com.sprint.mission.discodeit;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DiscodeitRepository.class)
public class DiscodeitConfig {

    private final DiscodeitRepository discodeitRepository;

    public DiscodeitConfig(DiscodeitRepository discodeitRepository) {
        this.discodeitRepository = discodeitRepository;
    }

    public String getFilePath(){
        return this.discodeitRepository.getPath();
    }

    public String getRepoType(){
        return this.discodeitRepository.getType();
    }

}

