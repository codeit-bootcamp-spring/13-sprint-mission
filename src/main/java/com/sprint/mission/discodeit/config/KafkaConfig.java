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

    static public final String MESSAGE_EVENT = "discodeit.event.message";
    static public final String ROLE_EVENT = "discodeit.event.role";
    static public final String SYSTEM_EVENT = "discodeit.event.system";
    static public final String USER_EVENT = "discodeit.event.user";
    static public final String CHANNEL_EVENT = "discodeit.event.channel";
    static public final String BINARY_EVENT = "discodeit.event.binary";
    static public final String WEBSOCKET_EVENT = "discodeit.event.websocket";


    private final KafkaProperties kafkaProperties;

    @PostConstruct
    public void checkConfiguration() {
        log.info(
                "카프카 브로커 설정 : {}",
                kafkaProperties.getBootstrapServers()
        );
    }
}
