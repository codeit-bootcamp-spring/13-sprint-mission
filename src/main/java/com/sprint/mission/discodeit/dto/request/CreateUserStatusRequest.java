package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record CreateUserStatusRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        @PastOrPresent (message = "마지막 접속 시간이 미래 시점일 수 없습니다.")
        Instant lastOnlineAt
) {
    public CreateUserStatusRequest{
        if (lastOnlineAt == null) {
            lastOnlineAt = Instant.now();
        }
    }

    public CreateUserStatusCommand toCommnad(){
        return new CreateUserStatusCommand(userId, lastOnlineAt);
    }

}
