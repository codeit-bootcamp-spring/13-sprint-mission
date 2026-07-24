package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

import java.util.*;

public record CreatePrivateChannelRequest(
        @NotEmpty(message = "참여자는 최소 1명 이상이여야 합니다.")
        List<@NotNull(message = "참여자 ID에는 null이 포함될 수 없습니다.") UUID> participantIds
) {

    public CreatePrivateChannelCommand toCommand() {
        return new CreatePrivateChannelCommand(participantIds);
    }
}
