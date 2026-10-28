package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * 도메인 DTO를 JSON 스냅샷과 수신 대상 정보로 고정한 뒤 저장소에 발행을 요청한다.
 * 현재 접속자가 없어도 이벤트를 저장하므로 재접속 시 수신자별 이력을 복원할 수 있다.
 * 전송 방식 선택은 저장소 구현체에 맡겨 서비스가 Redis 여부에 의존하지 않게 한다.
 */
@Component
@RequiredArgsConstructor
public class DefaultSseEventPublisher implements SseEventPublisher{

    private final ObjectMapper objectMapper;
    private final SseMessageRepository messageRepository;

    @Override
    public void send(Collection<UUID> receiverIds, String eventName, Object data) {
        Set<UUID> receivers = Set.copyOf(receiverIds);
        if (receivers.isEmpty()){
            return;
        }

        publish(receivers, eventName, data, false);
    }

    @Override
    public void broadcast(String eventName, Object data) {
        // 전체 방송은 현재 접속자 목록을 저장하지 않는다. 나중에 재접속한 사용자도 복원할 수 있다.
        publish(Set.of(), eventName, data, true);
    }

    /**
     * 수신 대상과 JSON 본문을 고정하고 새 이벤트 ID를 부여해 저장·발행한다.
     * 직렬화에 실패하면 저장 전 예외가 발생한다.
     */
    private void publish(Set<UUID> receiverIds, String eventName, Object data, boolean broadcast){
        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE 이벤트를 JSON으로 변환할 수 없습니다.", e);
        }

        SseMessage message = new SseMessage(UUID.randomUUID(), eventName, dataJson, receiverIds, broadcast);
        messageRepository.saveAndPublish(message);
    }
}
