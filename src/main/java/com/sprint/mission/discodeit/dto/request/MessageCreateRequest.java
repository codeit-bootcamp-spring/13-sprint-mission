package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(

    @NotBlank(message = "message를 입력해주세요.")
    @Size(max = 1000)
    String content,

    @NotNull(message = "채널을 선택해주세요.")
    UUID channelId,

    @NotNull
    UUID authorId,
    List<UUID> attachmentIds) {

}
