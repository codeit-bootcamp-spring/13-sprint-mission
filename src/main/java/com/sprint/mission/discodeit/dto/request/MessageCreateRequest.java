package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String content,

        @NotNull(message = "channelId는 필수입니다.")
        UUID channelId,

        @NotNull(message = "authorId는 필수입니다.")
        UUID authorId,

        List<String> attachmentPathList
) {
}
