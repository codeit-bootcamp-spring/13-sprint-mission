package com.sprint.mission.discodeit.event;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdateEvent;
import com.sprint.mission.discodeit.event.channel.ChannelEvent;
import com.sprint.mission.discodeit.event.user.UserEvent;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sprint.mission.discodeit.config.cache.CacheConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final ObjectMapper objectMapper;
    private final SseService sseService;


    // 유저 정보 변경에 대한 이벤트
    @KafkaListener(topics = "discodeit.event.user")
    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true) // todo - 특정 유저 상태만 바꾸도록 변경. 필요하나?
    public void userEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                UserEvent userEvent = objectMapper.readValue(event, UserEvent.class);

                // user 상태정보 업데이트.
                sseService.broadcast(userEvent.getName(), userEvent.getData());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","UserEvent",event);
            throw new RuntimeException(e);
        }
    }


    // 채널 정보 변경에 대한 이벤트
    @KafkaListener(topics = "discodeit.event.channel")
    public void channelEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                ChannelEvent channelEvent = objectMapper.readValue(event, ChannelEvent.class);

                sseService.broadcast(channelEvent.getName(), channelEvent.getData());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","ChannelEvent",event);
            throw new RuntimeException(e);
        }
    }

    // 채널 정보 변경에 대한 이벤트
    @KafkaListener(topics = "discodeit.event.binary")
    public void binaryEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                BinaryContentUpdateEvent binaryEvent = objectMapper.readValue(event, BinaryContentUpdateEvent.class);

                sseService.broadcast(binaryEvent.getName(), binaryEvent.getDto());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","BinaryEvent",event);
            throw new RuntimeException(e);
        }
    }



//    private Object parseEvent(String event, Class<?> cls) {
//        try {
//            return objectMapper.readValue(event, cls);
//        } catch (JsonProcessingException e) {
//            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","UserEvent",event);
//            throw new RuntimeException(e);
//        }
//    }

}
