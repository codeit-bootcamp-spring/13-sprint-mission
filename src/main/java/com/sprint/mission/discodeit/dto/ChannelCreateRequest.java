package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChannelCreateRequest {

    /*
     * /api/channels/public 경로에서 컨트롤러가
     * ChannelType.PUBLIC 값을 직접 넣어주므로
     * 요청 본문에서는 type이 null이어도 허용한다.
     */
    private ChannelType type;

    @NotBlank(message = "채널 이름은 필수입니다.")
    @Size(
            max = 100,
            message = "채널 이름은 100자 이하여야 합니다."
    )
    @JsonAlias({"title", "channelName"})
    private String name;

    @Size(
            max = 500,
            message = "채널 설명은 500자 이하여야 합니다."
    )
    @JsonAlias({"desc"})
    private String description;

    public ChannelCreateRequest(
            ChannelType type,
            String name,
            String description
    ) {
        this.type = type;
        this.name = name;
        this.description = description;
    }
}