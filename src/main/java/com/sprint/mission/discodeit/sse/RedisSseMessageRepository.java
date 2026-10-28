package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 모든 인스턴스가 사용하는 SSE 이력·순번을 Redis에 저장하고 Pub/Sub으로 알린다.
 * Lua로 중복 확인, 순번 증가, 이력 저장, 발행을 한 번에 실행한다.
 * 원자성의 범위는 해당 Redis 실행이며 업무 DB 커밋과의 원자성까지 보장하지 않는다.
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisSseMessageRepository implements SseMessageRepository {

    public static final String SSE_EVENTS_TOPIC = "discodeit:sse:events";

    private static final String SEQUENCE_KEY = "discodeit:sse:history:sequence";
    private static final String ORDER_KEY = "discodeit:sse:history:order";
    private static final String MESSAGES_KEY = "discodeit:sse:history:messages";

    private static final RedisScript<Long> SAVE_AND_PUBLISH_SCRIPT = RedisScript.of(
            new ClassPathResource("redis/save-and-publish-sse-message.lua"),
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Lua가 이력을 저장한 다음 Pub/Sub 알림을 발행하도록 요청한다.
     * 반환값 1은 신규 저장, 0은 중복이며 그 외 결과는 저장 실패로 취급한다.
     */
    @Override
    public boolean saveAndPublish(SseMessage message){
        String messageJson;

        try {
           messageJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE 이벤트 이력을 JSON으로 변환할 수 없습니다.", e);
        }

        Long result = redisTemplate.execute(
                SAVE_AND_PUBLISH_SCRIPT,
                List.of(SEQUENCE_KEY, ORDER_KEY, MESSAGES_KEY),
                message.eventId().toString(),
                messageJson,
                SSE_EVENTS_TOPIC
        );

        if (result == null || (result != 0L && result != 1L)){
            throw new IllegalStateException("Redis SSE 이벤트 저장 결과가 올바르지 않습니다.");
        }
        return result == 1L;
    }

    /**
     * 커서 다음 순번부터 조회한 뒤 해당 사용자가 받을 이벤트만 남긴다.
     * 알 수 없는 커서에는 빈 이력을 반환하며 임의로 전체 이력을 재생하지 않는다.
     */
    @Override
    public List<SseMessage> findAllAfter(UUID receiverId, UUID lastEventId){

        OptionalLong lastSequence = findSequence(lastEventId);
        if(lastSequence.isEmpty()){
            return List.of();
        }

        Set<String> eventIds = redisTemplate.opsForZSet().rangeByScore(
                ORDER_KEY,
                lastSequence.getAsLong() + 1,
                Double.POSITIVE_INFINITY
        );

        if (eventIds == null || eventIds.isEmpty()){
            return List.of();
        }

        List<String> messageJsons = redisTemplate.<String, String>opsForHash()
                .multiGet(MESSAGES_KEY, eventIds);

        return messageJsons.stream()
                .filter(Objects::nonNull)
                .map(this::convertJsonToMessage)
                .filter(message -> message.broadcast() || message.receiverIds().contains(receiverId))
                .toList();
    }

    @Override
    public OptionalLong findSequence(UUID eventId){
        if (eventId == null){
            return OptionalLong.empty();
        }

        Double sequence = redisTemplate.opsForZSet().score(ORDER_KEY, eventId.toString());

        if (sequence == null){
            return OptionalLong.empty();
        }

        return OptionalLong.of(sequence.longValue());
    }

    private SseMessage convertJsonToMessage(String json) {
        try {
            return objectMapper.readValue(json, SseMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Redis SSE 이벤트 이력을 읽을 수 없습니다.", e);
        }
    }


}
