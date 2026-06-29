package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChannelUpdateRequest {

    private String name;
    private String description;

    // 타입은 항상 PUBLIC 으로 유지되므로 dto에선 제거

}
