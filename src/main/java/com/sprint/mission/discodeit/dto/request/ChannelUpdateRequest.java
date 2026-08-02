package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelUpdateRequest(

        @NotBlank(message = "채널 이름은 필수입니다.")
        @Size(max = 20, message = "채널 이름은 20자 이하로 입력해주세요.")
        String channelName,

        @Size(max = 100, message = "채널 설명은 100자 이하로 입력해주세요.")
        String description
) {
}
