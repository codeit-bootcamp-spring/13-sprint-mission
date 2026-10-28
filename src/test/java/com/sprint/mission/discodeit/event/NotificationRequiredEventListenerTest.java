package com.sprint.mission.discodeit.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredEventListenerTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private NotificationRequiredEventListener listener;

  @Test
  @DisplayName("알림을 켠 사용자에게만 알림을 보내고 작성자는 제외한다")
  void handleMessageCreated_OnlyEnabledRecipients() {
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(null, ChannelType.PRIVATE, null);

    User author = createUser("author");
    User receiver = createUser("receiver");
    User disabledUser = createUser("disabled");

    ReadStatus authorStatus =
        new ReadStatus(author, channel, Instant.now());

    ReadStatus receiverStatus =
        new ReadStatus(receiver, channel, Instant.now());

    ReadStatus disabledStatus =
        new ReadStatus(disabledUser, channel, Instant.now());

    disabledStatus.updateNotificationEnabled(false);

    given(readStatusRepository.findByChannelId(channelId))
        .willReturn(List.of(authorStatus, receiverStatus, disabledStatus));

    MessageCreatedEvent event = new MessageCreatedEvent(
        channelId,
        author.getId(),
        "author",
        "개인 채널",
        "안녕하세요",
        null
    );

    listener.handleMessageCreated(event);

    then(notificationService).should().create(
        receiver.getId(),
        "author님의 새 메시지 (개인 채널)",
        "안녕하세요"
    );

    verifyNoMoreInteractions(notificationService);
  }

  @Test
  @DisplayName("본문 없는 메시지는 기본 알림 문구를 사용한다")
  void handleMessageCreated_WithoutContent() {
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(null, ChannelType.PRIVATE, null);
    User receiver = createUser("receiver");

    ReadStatus readStatus =
        new ReadStatus(receiver, channel, Instant.now());

    given(readStatusRepository.findByChannelId(channelId))
        .willReturn(List.of(readStatus));

    MessageCreatedEvent event = new MessageCreatedEvent(
        channelId,
        UUID.randomUUID(),
        "author",
        "개인 채널",
        null,
        null
    );

    listener.handleMessageCreated(event);

    then(notificationService).should().create(
        receiver.getId(),
        "author님의 새 메시지 (개인 채널)",
        "새 메시지가 도착했습니다."
    );
  }

  @Test
  @DisplayName("권한이 변경된 사용자에게 변경 알림을 생성한다")
  void handleRoleUpdated_Success() {
    UUID userId = UUID.randomUUID();

    RoleUpdatedEvent event = new RoleUpdatedEvent(
        userId,
        Role.USER,
        Role.CHANNEL_MANAGER
    );

    listener.handleRoleUpdated(event);

    then(notificationService).should().create(
        userId,
        "사용자 권한 변경",
        "권한이 USER에서 CHANNEL_MANAGER로 변경되었습니다."
    );
  }

  private User createUser(String username) {
    User user = new User(
        username + "@test.com",
        username,
        "encodedPassword"
    );

    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    return user;
  }
}