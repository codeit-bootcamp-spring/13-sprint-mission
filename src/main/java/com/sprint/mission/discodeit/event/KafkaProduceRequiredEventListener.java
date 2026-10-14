package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.event.system.SystemNotifyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("noty")
    @TransactionalEventListener
    public void on(MessageCreateEvent event) {
        log.debug("카프카 - 메세지 생성 이벤트 발행.");
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);
        }catch(JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }

    @Async("noty")
    @TransactionalEventListener
    public void on(RoleUpdateEvent event) {
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.RoleUpdateEvent", payload);
        }catch(JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }

    @Async("noty")
    @EventListener
    public void on(SystemNotifyEvent event) {
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.SystemNotifyEvent", payload);
        }catch(JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }
}

