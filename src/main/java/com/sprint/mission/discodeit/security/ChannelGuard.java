package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.command.channel.ChannelCreateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.service.basic.ChannelReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelGuard {

    private final ChannelReader channelReader;

    public boolean isUserInPrivateChannelParticipants(ChannelCreateCommand command, UUID userId) {
        if (userId == null ) {
            return false;
        }

       return getPrivateChannelCommand(command)
                .filter(ChannelCreatePrivateCommand::isPrivate)
                .filter(privateCommand -> privateCommand.participantIds() !=null)
                .map(privateCommand -> privateCommand.participantIds().contains(userId))
                .orElse(false);
    }

    public boolean isAccess(UUID channelId, UUID userId) {
        if (channelId == null || userId == null) {
            return false;
        }
        return channelReader.isChannelAccessible(channelId, userId);
    }

    private Optional<ChannelCreatePrivateCommand> getPrivateChannelCommand(ChannelCreateCommand command) {
        if (command instanceof ChannelCreatePrivateCommand privateCommand) {
            return Optional.of(privateCommand);
        }
        return Optional.empty();
    }
}
