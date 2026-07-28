package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.command.channel.PublicChannelCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PublicChannelRequest(
        @Schema(description = "공개 채널 이름", example = "공지 채널", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "채널명을 입력해주세요.")
        String name,
        @Schema(description = "채널 설명", example = "공지를 위한 채널입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "채널 설명을 입력해주세요.")
        String description
)
{
    public PublicChannelCommand toCommand(){
        return new PublicChannelCommand(name, description);
    }
}
