package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record ChannelRequest(
) {

    public record CreatePublicChannel(
            String name,
            String description
    ) {
    }

    public record CreatePrivateChannel(
            List<UUID> participantIds
    ) {
    }

    public record UpdateChannel(
            UUID channelId,
            String name,
            String description
    ) {
    }

}
