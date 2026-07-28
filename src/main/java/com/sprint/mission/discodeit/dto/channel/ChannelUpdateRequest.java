package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record ChannelUpdateRequest(
        @Schema(description = "변경 할 채널 이름", example = "소개 채널", requiredMode = Schema.RequiredMode.REQUIRED)
        @Pattern(regexp = "\\S+", message = "채널명은 공백일 수 없습니다.")
        String name,
        @Schema(description = "변경 할 채널 설명", example = "소개 채널 입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Pattern(regexp = "\\S+", message = "채널설명은 공백일 수 없습니다.")
        String description

)
{
    public ChannelUpdateCommand toCommand() {
        return new ChannelUpdateCommand(name, description);
    }
}
