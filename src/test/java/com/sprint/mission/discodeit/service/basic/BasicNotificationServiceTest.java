package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.NotificationNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private BasicNotificationService notificationService;

  private UUID receiverId;
  private UUID notificationId;
  private Notification notification;

  @BeforeEach
  void setUp() {
    receiverId = UUID.randomUUID();
    notificationId = UUID.randomUUID();

    User receiver = new User(
        "receiver@test.com",
        "receiver",
        "encodedPassword"
    );

    ReflectionTestUtils.setField(receiver, "id", receiverId);

    notification = new Notification(receiver, "알림 제목", "알림 내용");
    ReflectionTestUtils.setField(notification, "id", notificationId);
  }

  @Test
  @DisplayName("알림 수신자는 자신의 알림을 삭제할 수 있다")
  void delete_Success_Owner() {
    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(notification));

    notificationService.delete(notificationId, receiverId);

    then(notificationRepository).should().delete(notification);
  }

  @Test
  @DisplayName("다른 사용자는 알림을 삭제할 수 없다")
  void delete_Fail_NotOwner() {
    UUID otherUserId = UUID.randomUUID();

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(notification));

    assertThatThrownBy(
        () -> notificationService.delete(notificationId, otherUserId)
    ).isInstanceOf(NotificationAccessDeniedException.class);

    then(notificationRepository).should(never()).delete(any());
  }

  @Test
  @DisplayName("존재하지 않는 알림은 삭제할 수 없다")
  void delete_Fail_NotFound() {
    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.empty());

    assertThatThrownBy(
        () -> notificationService.delete(notificationId, receiverId)
    ).isInstanceOf(NotificationNotFoundException.class);

    then(notificationRepository).should(never()).delete(any());
  }
}