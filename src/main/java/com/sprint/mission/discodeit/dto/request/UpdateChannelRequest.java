package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Channel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateChannelRequest {

    private UUID channelId;
    private String name;
    private Channel.ChannelType type;
    private String description;

}
