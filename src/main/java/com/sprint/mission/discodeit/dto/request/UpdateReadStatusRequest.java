package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;

import java.time.*;
@Schema(description = "유저 접속 상태 정보")
public record UpdateReadStatusRequest  (

        @PastOrPresent(message = "마지막 접속 시간이 미래 시점일 수 없습니다.")
        Instant lastReadTime
) {
    public UpdateReadStatusRequest{
        if (lastReadTime == null) {
            lastReadTime = Instant.now();
        }
    }

    public UpdateReadStatusCommand toCommand(){
        return new UpdateReadStatusCommand(lastReadTime);
    }
}
