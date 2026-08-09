package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

        @NotEmpty(message = "Private 채널은 최소 1명 이상의 참여자가 필요합니다.")
        List<UUID> participantIds
) {

}