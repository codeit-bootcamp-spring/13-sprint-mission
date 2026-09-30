package com.sprint.mission.discodeit.config.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalConfig {

    @Value(value = "${STORAGE_LOCAL_ROOT_PATH:.discodeit/storage}")
    private Path root;

    @PostConstruct
    void init(){

        log.debug("LocalStorage - local storage check - {}", root);

        if (Files.notExists(root)){
            try {

                Files.createDirectories(root);

                log.debug("LocalStorage - folder created - {}", root);

            } catch (IOException e) { throw new RuntimeException(e); }
        }
    }

    // 로컬 경로 resolve 용
    public Path resolvePath(String path){
        return root.resolve(path);
    }

}
