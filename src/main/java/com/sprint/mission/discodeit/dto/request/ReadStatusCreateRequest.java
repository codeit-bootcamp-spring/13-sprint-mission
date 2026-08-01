package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull(message = "userId를 입력해주세요.")
    UUID userId,
    @NotNull(message = "channelId를 입력해주세요.")
    UUID channelId,
    @NotNull(message = "마지막 읽은 시간을 입력해주세요.")
    Instant lastReadAt) {

}