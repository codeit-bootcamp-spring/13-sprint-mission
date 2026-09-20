package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(
            max = 1000,
            message = "메시지는 1000자 이하여야 합니다."
    )
    private String content;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    @NotNull(message = "채널 ID는 필수입니다.")
    private UUID channelId;

    public String getContent() {
        return content;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getChannelId() {
        return channelId;
    }
}