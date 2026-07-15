package com.sprint.mission.discodeit.dto.command.channel;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCommand(
        List<UUID> participantIds
){
}
