package com.sprint.mission.discodeit.event.binarycontent;

import java.util.UUID;

/** BinaryContent 메타데이터 저장 후 발행하는 파일 저장 이벤트. */
public record BinaryContentCreatedEvent(UUID binaryContentId, byte[] bytes) {
}
