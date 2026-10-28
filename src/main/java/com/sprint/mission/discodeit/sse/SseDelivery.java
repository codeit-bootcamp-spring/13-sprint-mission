package com.sprint.mission.discodeit.sse;

import java.util.UUID;

/**
 * SSE 연결에 넘기는 전송 자료다. eventId는 외부 복원 커서, sequence는 내부 정렬 기준이다.
 * data에는 저장된 JSON을 복원한 값을 담아 SSE 직렬화 시 JSON 문자열이 중첩되지 않게 한다.
 */
public record SseDelivery(UUID eventId, long sequence, String eventName, Object data) {
}
