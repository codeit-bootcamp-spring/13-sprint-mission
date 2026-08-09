package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

@Schema(description = "유저 마지막 접속 시간 정보")
public record UpdateUserStatusRequest(
        @PastOrPresent (message = "마지막 접속 시간이 미래 시점일 수 없습니다.")
        Instant lastOnlineTime
) {
    public UpdateUserStatusRequest{
        if (lastOnlineTime == null) {
            lastOnlineTime = Instant.now();
        }
    }

    public UpdateUserStatusCommand toCommand(){
        return new UpdateUserStatusCommand(lastOnlineTime);
    }
}
