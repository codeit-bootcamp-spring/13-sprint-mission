package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SseEventListener {

    private final SseService sseService;
    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        sseService.send(
                List.of(event.receiverId()),
                "notifications.created",
                event.notificationDto()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BinaryContentUpdatedEvent event) {
        sseService.broadcast(
                "binaryContents.updated",
                event.binaryContentDto()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        if (event.receiverIds() == null) {
            sseService.broadcast(
                    "channels.created",
                    event.channelDto()
            );
        } else {
            sseService.send(
                    event.receiverIds(),
                    "channels.created",
                    event.channelDto()
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        sseService.broadcast(
                "channels.updated",
                event.channelDto()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        if (event.receiverIds() == null) {
            sseService.broadcast(
                    "channels.deleted",
                    event.channelDto()
            );
        } else {
            sseService.send(
                    event.receiverIds(),
                    "channels.deleted",
                    event.channelDto()
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserCreatedEvent event) {
        sseService.broadcast(
                "users.created",
                event.userDto()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserUpdatedEvent event) {
        sseService.broadcast(
                "users.updated",
                event.userDto()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserDeletedEvent event) {
        sseService.broadcast(
                "users.deleted",
                event.userDto()
        );
    }

    @EventListener
    public void on(UserLogInOutEvent event) {
        UserDto userDto = userService.getUser(event.userId());

        sseService.broadcast(
                "users.updated",
                userDto
        );
    }
}
