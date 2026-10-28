package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * 저장된 JSON 이벤트에 순번을 결합하여 실제 SSE 전송 자료로 변환한다.
 * JSON 문자열을 JsonNode로 복원해 클라이언트가 문자열이 아닌 JSON 객체를 받도록 한다.
 */
@Component
@RequiredArgsConstructor
public class SseMessageReader {

    private final SseMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public OptionalLong findSequence(UUID eventId){
        return messageRepository.findSequence(eventId);
    }

    public List<SseDelivery> findDeliveriesAfter(UUID receiverId, UUID lastEventId){
        return messageRepository.findAllAfter(receiverId, lastEventId)
                .stream()
                .map(this::toDelivery)
                .toList();
    }

    /**
     * 저장 순번을 조회하고 JSON 본문을 전송 가능한 값으로 복원한다.
     * 순번이 없거나 JSON이 손상되면 불완전한 이벤트를 보내지 않고 예외를 발생시킨다.
     */
    public SseDelivery toDelivery(SseMessage message){
        long sequence = messageRepository.findSequence(message.eventId())
                .orElseThrow(() -> new IllegalStateException("SSE 이벤트 순번을 찾을 수 없습니다. eventId=" + message.eventId()));

        return new SseDelivery(
                message.eventId(),
                sequence,
                message.eventName(),
                convertTojsonNode(message)
        );
    }

    private JsonNode convertTojsonNode(SseMessage message){
        try {
            return objectMapper.readTree(message.dataJson());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE 이벤트 데이터를 읽을 수 없습니다.", e);
        }
    }
}
