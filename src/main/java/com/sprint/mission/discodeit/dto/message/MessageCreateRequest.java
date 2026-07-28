package com.sprint.mission.discodeit.dto.message;


import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageCreateRequest(
        @Schema(description = "채널 ID", example = "383b89f5-3200-4504-9283-2d46cd007444", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "채널 id는 필수 값입니다.")
        UUID channelId,
        @Schema(description = "작성자 ID", example = "0df2cecb-ff53-49c0-a522-a6065eeac3b9", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "작성자 id는 필수 값입니다.")
        UUID authorId,
        @Schema(description = "메시지", example = "안녕하세요", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "메시지를 입력해주세요.")
        String content

)
{
    public MessageCreateCommand toCommand(){
        return new MessageCreateCommand(channelId, authorId, content);
    }
}
