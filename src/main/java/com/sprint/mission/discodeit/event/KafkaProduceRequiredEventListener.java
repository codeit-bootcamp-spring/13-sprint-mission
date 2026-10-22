package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.cache.CacheConfig;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdateEvent;
import com.sprint.mission.discodeit.event.channel.ChannelEvent;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.event.system.SystemNotifyEvent;
import com.sprint.mission.discodeit.event.user.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void on(MessageCreateEvent event) {
        log.debug("카프카 - 메세지 생성 이벤트 발행.");
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.event.message", payload);
            kafkaTemplate.send("discodeit.event.websocket", payload);
        } catch (JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }

    @Async("notificationExecutor")
    @TransactionalEventListener
    public void on(RoleUpdateEvent event) {
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.event.role", payload);
        }catch(JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void on(SystemNotifyEvent event) {
        try{
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.event.system", payload);
        }catch(JsonProcessingException e){
            log.error("json 파싱 에러",e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
//    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true) // 작동을 하나?
    public void on(UserEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event.getData());
            kafkaTemplate.send("discodeit.event.user", payload);
            log.debug("[UserEvent] {} 이벤트 발송- {}({})",
                    event.getName(),
                    event.getData().username(),
                    event.getData().id());
        } catch (JsonProcessingException e) {
            log.error("[UserEvent] {} 이벤트 발송 실패 - id : {}, data : {}...",
                    event.getName(),
                    event.getId(),
                    event.getData().email());
        }
    }

    @Async("contentRelateExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void update(BinaryContentUpdateEvent event){
       // 발송 대상자를 어떻게 판별?
//        sseService.broadcast(event.getName(),  event.getDto());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.event.binary", payload);
            log.debug("[BinaryEvent] {} 이벤트 발송- {}({})",
                    event.getName(),
                    event.getDto().fileName(),
                    event.getDto().id());
        } catch (JsonProcessingException e) {
            log.error("[BinaryEvent] {} 이벤트 발송 실패 - id : {}, data : {}...",
                    event.getName(),
                    event.getId(),
                    event.getDto().fileName());
        }
    }

    // 채널 정보 변경에 대한 이벤트 함수. 변경점이 커밋되고 난 후, 커밋을 보낸다.
    // todo - 알림 대상을 어떻게 분간? 채널에 따라 접속할 수 있는 유저를 파악해서 알림?
    @Async("notificationExecutor")
    @EventListener
    public void on(ChannelEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event.getData());
            kafkaTemplate.send("discodeit.event.channel", payload);
            log.debug("[ChannelEvent] {} 이벤트 발송- {}({})",
                    event.getName(),
                    event.getData().id(),
                    event.getData().name());
        } catch (JsonProcessingException e) {
            log.error("[ChannelEvent] {} 이벤트 발송 실패 - id : {}, data : {}...",
                    event.getName(),
                    event.getId(),
                    event.getData());
        }
    }

}

