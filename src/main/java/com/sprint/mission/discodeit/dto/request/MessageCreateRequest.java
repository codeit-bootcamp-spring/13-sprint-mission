package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(

        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID userId,

        @NotNull(message = "채널 ID는 필수입니다.")
        UUID channelId,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 1000, message = "메시지는 1000자 이하로 입력해주세요.")
        String content,

        @Valid
        List<BinaryContentCreateRequest> attachment
) {
}
