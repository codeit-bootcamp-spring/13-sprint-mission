package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateChannelRequest {

    private String name;
    private String description;

    // 타입은 항상 PUBLIC 으로 유지되므로 dto에선 제거

}
