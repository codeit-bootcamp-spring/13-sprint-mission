package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.*;

import java.time.*;
@Schema(description = "유저 마지막 접속 시간 정보")
public record UpdateUserStatusRequest(
        Instant lastOnlineTime
) {

}
