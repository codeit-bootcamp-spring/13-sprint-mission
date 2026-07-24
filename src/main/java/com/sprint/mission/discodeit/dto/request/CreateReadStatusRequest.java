package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

import java.time.*;
import java.util.*;

public record CreateReadStatusRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        @NotNull(message = "채널 ID는 필수입니다.")
        UUID channelId,

        @PastOrPresent(message = "읽은 시간이 현재시간보다 빠를 수 없습니다.")
        Instant lastReadAt
) {

    public CreateReadStatusRequest{
        if (lastReadAt == null) {
            lastReadAt = Instant.now();
        }
    }

    public CreateReadStatusCommand toCommand(){
        return new CreateReadStatusCommand(userId, channelId, lastReadAt);
    }
}

