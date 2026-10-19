package com.sprint.mission.discodeit.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @PostConstruct
    public void checkConfiguration() {
        log.info(
                "카프카 브로커 설정 : {}",
                kafkaProperties.getBootstrapServers()
        );
    }
}
