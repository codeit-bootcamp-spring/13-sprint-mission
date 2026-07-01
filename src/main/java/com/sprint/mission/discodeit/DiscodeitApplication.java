package com.sprint.mission.discodeit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class DiscodeitApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

    }
}
