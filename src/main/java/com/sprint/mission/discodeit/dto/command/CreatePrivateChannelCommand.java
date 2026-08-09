package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.util.*;

public record CreatePrivateChannelCommand(
        List<UUID> participantIds
) {

    public static CreatePrivateChannelCommand from(CreatePrivateChannelRequest request) {
        return new CreatePrivateChannelCommand(request.participantIds());
    }
}
