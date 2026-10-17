package com.sprint.mission.discodeit.event;

import java.util.Objects;
import java.util.UUID;

public record BinaryContentCreatedEvent(
        UUID binaryContentId,
        byte[] bytes
) {

    public BinaryContentCreatedEvent{

        Objects.requireNonNull(binaryContentId,
                "바이너리 콘텐츠 ID는 필수입니다.");

        Objects.requireNonNull(bytes,
                "바이너리 데이터는 필수입니다.");

        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }
}
