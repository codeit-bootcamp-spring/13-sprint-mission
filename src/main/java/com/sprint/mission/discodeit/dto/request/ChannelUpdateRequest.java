package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChannelUpdateRequest(

    @Pattern(
        regexp = ".*\\S.*",
        message = "채널 이름은 공백으로만 구성할 수 없습니다."
    )
    @Size(max = 100, message = "채널 이름은 100자 이하여야 합니다.")
    String newName,

    @Size(max = 500, message = "채널 설명은 500자 이하여야 합니다.")
    String newDescription
) {

}
