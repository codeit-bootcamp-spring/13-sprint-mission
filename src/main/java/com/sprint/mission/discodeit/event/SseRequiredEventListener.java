package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

  private final SseService sseService;

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreated(NotificationCreatedEvent event) {
    NotificationDto notificationDto = event.notificationDto();

    sseService.send(
        List.of(notificationDto.receiverId()),
        "notifications.created",
        notificationDto
    );
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentUpdated(BinaryContentUpdatedEvent event) {
    sseService.broadcast(
        "binaryContents.updated",
        event.binaryContentDto()
    );
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleChannelChanged(ChannelChangedEvent event) {
    ChannelDto channelDto = event.channelDto();

    if (channelDto.type() == ChannelType.PRIVATE) {
      sseService.send(
          channelDto.participants().stream()
              .map(UserDto::id)
              .toList(),
          event.eventName(),
          channelDto
      );
    } else {
      sseService.broadcast(
          event.eventName(),
          channelDto
      );
    }
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUserChanged(UserChangedEvent event) {
    sseService.broadcast(
        event.eventName(),
        event.userDto()
    );
  }
}