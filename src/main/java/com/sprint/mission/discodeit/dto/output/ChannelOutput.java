package com.sprint.mission.discodeit.dto.output;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
public class ChannelOutput {
    private final UUID channelID;
    private final String channelName;
    private final String channelDescription;
    private final Instant lastMsgTime;
    private final List<UUID> userIDs;

    @Override
    public String toString() {
        return "ChannelOutput{" +
                "channelID=" + channelID +
                ", channelName='" + channelName + '\'' +
                ", channelDescription='" + channelDescription + '\'' +
                ", lastMsgTime=" + lastMsgTime +
                ", userIDs=" + userIDs +
                '}';
    }
}
