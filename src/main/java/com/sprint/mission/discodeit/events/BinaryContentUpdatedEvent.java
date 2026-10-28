package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;

/**
 * 업로드 상태 변경 시점의 파일 DTO를 전달한다.
 * 소비자가 엔티티를 다시 직렬화하지 않고 커밋된 변경 내용을 사용할 수 있게 한다.
 */
public record BinaryContentUpdatedEvent(BinaryContentDto binaryContent) {
}
