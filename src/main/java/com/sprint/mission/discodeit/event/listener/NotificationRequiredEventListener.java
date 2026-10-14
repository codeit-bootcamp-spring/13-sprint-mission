package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRequiredEventListener {

  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public void handle(MessageCreatedEvent event) {

    Message message = messageRepository
        .findWithAuthorAndChannelById(event.messageId())
        .orElseThrow();

    if (message.getAuthor() == null) {
      return;
    }

    UUID authorId = message.getAuthor().getId();
    UUID channelId = message.getChannel().getId();

    List<ReadStatus> readStatuses =
        readStatusRepository.findByChannelIdAndNotificationEnabledTrue(channelId);

    String channelName = message.getChannel().getName() != null
        ? message.getChannel().getName()
        : "DM";

    String title = message.getAuthor().getUsername()
        + " (#" + channelName + ")";

    String content = message.getContent() != null
        ? message.getContent()
        : "첨부파일을 보냈습니다.";

    int count = 0;

    for (ReadStatus readStatus : readStatuses) {
      UUID receiverId = readStatus.getUser().getId();

      if (receiverId.equals(authorId)) {
        continue;
      }

      notificationService.create(receiverId, title, content);
      count++;
    }

    log.info("메세지 알림 생성 완료: messageId={}, notificationCount={}", event.messageId(), count);
  }

  public void handle(RoleUpdatedEvent event) {

    String title = "권한이 변경되었습니다.";

    String content = String.format(
        "%s -> %s",
        event.oldRole(),
        event.newRole()
    );

    notificationService.create(event.userId(), title, content);

    log.info(
        "권한 변경 알림 생성 완료: userId={}, oldRole={}, newRole={}",
        event.userId(),
        event.oldRole(),
        event.newRole()
    );
  }

  public void handle(S3UploadFailedEvent event) {

    String title = "S3 업로드 실패";

    String content = String.format(
        "작업: %s, Request ID: %s, 오류: %s",
        event.operation(),
        event.requestId(),
        event.errorMessage()
    );

    List<User> admins = userRepository.findByRole(Role.ADMIN);

    for (User admin : admins) {
      notificationService.create(admin.getId(), title, content);
    }

    log.error(
        "S3 업로드 실패 관리자 알림 생성 완료: binaryContentId={}, adminCount={}",
        event.binaryContentId(),
        admins.size());
  }
}
