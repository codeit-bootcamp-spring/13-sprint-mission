package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private BasicNotificationService notificationService;

    @Nested
    @DisplayName("Notification 목록 조회")
    class GetNotifications {

        @Test
        @DisplayName("사용자의 알림 목록을 DTO로 반환")
        void get_success() {
            // given
            UUID userId = UUID.randomUUID();

            Notification notification1 = mock(Notification.class);
            Notification notification2 = mock(Notification.class);

            NotificationDto dto1 = mock(NotificationDto.class);
            NotificationDto dto2 = mock(NotificationDto.class);

            given(notificationRepository.findAllByReceiverId(userId))
                    .willReturn(List.of(
                            notification1,
                            notification2
                    ));

            given(notificationMapper.toDto(notification1))
                    .willReturn(dto1);

            given(notificationMapper.toDto(notification2))
                    .willReturn(dto2);

            // when
            List<NotificationDto> result =
                    notificationService.getNotifications(userId);

            // then
            assertThat(result)
                    .containsExactly(dto1, dto2);

            then(notificationRepository).should()
                    .findAllByReceiverId(userId);

            then(notificationMapper).should()
                    .toDto(notification1);

            then(notificationMapper).should()
                    .toDto(notification2);
        }

        @Test
        @DisplayName("알림이 없으면 빈 목록을 반환")
        void get_empty() {
            // given
            UUID userId = UUID.randomUUID();

            given(notificationRepository.findAllByReceiverId(userId))
                    .willReturn(List.of());

            // when
            List<NotificationDto> result =
                    notificationService.getNotifications(userId);

            // then
            assertThat(result)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("Notification 삭제")
    class DeleteNotification {

        @Test
        @DisplayName("본인의 알림이면 삭제")
        void delete_success() {
            // given
            UUID notificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User receiver = mock(User.class);
            Notification notification = mock(Notification.class);

            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.of(notification));

            given(notification.getReceiver())
                    .willReturn(receiver);

            given(receiver.getId())
                    .willReturn(userId);

            // when
            notificationService.deleteNotification(
                    notificationId,
                    userId
            );

            // then
            then(notificationRepository).should()
                    .findById(notificationId);

            then(notificationRepository).should()
                    .deleteById(notificationId);
        }

        @Test
        @DisplayName("알림이 존재하지 않으면 예외 발생")
        void delete_fail_not_found() {
            // given
            UUID notificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> notificationService.deleteNotification(
                            notificationId,
                            userId
                    )
            ).isInstanceOf(
                    NotificationNotFoundException.class
            );

            then(notificationRepository).should(never())
                    .deleteById(notificationId);
        }

        @Test
        @DisplayName("다른 사용자의 알림이면 예외 발생")
        void delete_fail_access_denied() {
            // given
            UUID notificationId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID receiverId = UUID.randomUUID();

            User receiver = mock(User.class);
            Notification notification = mock(Notification.class);

            given(notificationRepository.findById(notificationId))
                    .willReturn(Optional.of(notification));

            given(notification.getReceiver())
                    .willReturn(receiver);

            given(receiver.getId())
                    .willReturn(receiverId);

            // when & then
            assertThatThrownBy(
                    () -> notificationService.deleteNotification(
                            notificationId,
                            userId
                    )
            ).isInstanceOf(
                    NotificationAccessDeniedException.class
            );

            then(notificationRepository).should(never())
                    .deleteById(notificationId);
        }
    }
}