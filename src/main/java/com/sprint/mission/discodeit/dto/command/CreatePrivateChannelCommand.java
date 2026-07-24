package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.util.*;

public record CreatePrivateChannelCommand(
        List<UUID> participantIds
) {

    public CreatePrivateChannelCommand from(CreatePrivateChannelRequest request) {
        return new CreatePrivateChannelCommand(request.participantIds());
    }
}
