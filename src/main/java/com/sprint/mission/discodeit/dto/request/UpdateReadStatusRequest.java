package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.*;

import java.time.*;
@Schema(description = "유저 접속 상태 정보")
public record UpdateReadStatusRequest  (
        Instant lastReadTime
) {
}
