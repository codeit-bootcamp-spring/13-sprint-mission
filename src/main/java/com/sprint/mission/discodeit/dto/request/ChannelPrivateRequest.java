package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ChannelPrivateRequest(
        @NotEmpty(message = "PRIVATE 채널에는 참여자가 한 명 이상 필요합니다.")
        List<@NotNull(message = "참여자 ID는 null일 수 없습니다.") UUID> channelIds
) {}