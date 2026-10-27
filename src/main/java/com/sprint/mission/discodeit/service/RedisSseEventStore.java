package com.sprint.mission.discodeit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.SseEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSseEventStore {

  private static final String KEY_PREFIX = "discodeit:sse:events:";
  private static final int MAX_EVENTS = 100;
  private static final Duration TTL = Duration.ofHours(24);

  private static final DefaultRedisScript<Long> SAVE_SCRIPT;

  static {
    SAVE_SCRIPT = new DefaultRedisScript<>();
    SAVE_SCRIPT.setResultType(Long.class);
    SAVE_SCRIPT.setScriptText("""
        redis.call('RPUSH', KEYS[1], ARGV[1])
        redis.call('LTRIM', KEYS[1], -tonumber(ARGV[2]), -1)
        redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
        return 1
        """);
  }

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void save(SseEvent event) {
    try {
      String json = objectMapper.writeValueAsString(event);

      Long result = redisTemplate.execute(
          SAVE_SCRIPT,
          List.of(key(event.receiverId())),
          json,
          String.valueOf(MAX_EVENTS),
          String.valueOf(TTL.toSeconds())
      );

      if (!Long.valueOf(1L).equals(result)) {
        throw new IllegalStateException("SSE 이벤트 Redis 저장 실패");
      }

    } catch (JsonProcessingException e) {
      throw new IllegalStateException("SSE 이벤트 직렬화 실패", e);
    }
  }

  public List<SseEvent> findAfter(
      UUID receiverId,
      UUID lastEventId
  ) {
    if (lastEventId == null) {
      return List.of();
    }

    List<String> stored = redisTemplate.opsForList()
        .range(key(receiverId), 0, -1);

    if (stored == null || stored.isEmpty()) {
      return List.of();
    }

    List<SseEvent> events = new ArrayList<>();
    int lastIndex = -1;

    for (String json : stored) {
      try {
        SseEvent event = objectMapper.readValue(
            json, SseEvent.class
        );

        if (!receiverId.equals(event.receiverId())) {
          continue;
        }

        events.add(event);

        if (lastEventId.equals(event.id())) {
          lastIndex = events.size() - 1;
        }

      } catch (JsonProcessingException e) {
        throw new IllegalStateException(
            "SSE 이벤트 역직렬화 실패", e
        );
      }
    }

    if (lastIndex < 0) {
      log.warn(
          "SSE 복원 기준 이벤트 없음: receiverId={}, lastEventId={}",
          receiverId, lastEventId
      );
      return List.of();
    }

    return List.copyOf(
        events.subList(lastIndex + 1, events.size())
    );
  }

  private String key(UUID receiverId) {
    return KEY_PREFIX + receiverId;
  }
}
