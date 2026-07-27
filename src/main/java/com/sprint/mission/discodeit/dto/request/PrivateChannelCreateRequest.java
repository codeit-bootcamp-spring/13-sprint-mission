package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

    @NotEmpty(message = "비공개 채널 참여자는 한 명 이상이어야 합니다.")
    List<@NotNull(message = "참여자 ID는 null일 수 없습니다.") UUID> participantIds
) {

}
