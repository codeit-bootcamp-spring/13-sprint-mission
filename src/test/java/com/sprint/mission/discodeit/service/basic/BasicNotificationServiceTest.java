package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private BasicNotificationService notificationService;

    @Test
    void create_알림을_생성한다() {
        UUID receiverId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        User receiver = new User(
                "receiver",
                "receiver@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(
                receiver,
                "id",
                receiverId
        );

        NotificationDto expected = new NotificationDto(
                notificationId,
                Instant.now(),
                receiverId,
                "권한이 변경되었습니다.",
                "USER -> CHANNEL_MANAGER"
        );

        given(userRepository.findById(receiverId))
                .willReturn(Optional.of(receiver));

        given(notificationRepository.save(
                any(Notification.class)
        )).willAnswer(
                invocation -> invocation.getArgument(0)
        );

        given(notificationMapper.toDto(
                any(Notification.class)
        )).willReturn(expected);

        NotificationDto actual = notificationService.create(
                receiverId,
                "권한이 변경되었습니다.",
                "USER -> CHANNEL_MANAGER"
        );

        assertThat(actual).isEqualTo(expected);

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        then(notificationRepository)
                .should()
                .save(captor.capture());

        Notification notification = captor.getValue();

        assertThat(notification.getReceiver())
                .isSameAs(receiver);
        assertThat(notification.getTitle())
                .isEqualTo("권한이 변경되었습니다.");
        assertThat(notification.getContent())
                .isEqualTo("USER -> CHANNEL_MANAGER");
    }

    @Test
    void findAllByReceiverId_사용자의_알림을_조회한다() {
        UUID receiverId = UUID.randomUUID();

        User receiver = new User(
                "receiver",
                "receiver@example.com",
                "encoded-password"
        );

        Notification firstNotification =
                new Notification(
                        receiver,
                        "첫 번째 알림",
                        "첫 번째 내용"
                );

        Notification secondNotification =
                new Notification(
                        receiver,
                        "두 번째 알림",
                        "두 번째 내용"
                );

        NotificationDto firstDto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "첫 번째 알림",
                "첫 번째 내용"
        );

        NotificationDto secondDto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "두 번째 알림",
                "두 번째 내용"
        );

        given(
                notificationRepository
                        .findAllByReceiverIdOrderByCreatedAtDesc(
                                receiverId
                        )
        ).willReturn(
                List.of(
                        firstNotification,
                        secondNotification
                )
        );

        given(notificationMapper.toDto(firstNotification))
                .willReturn(firstDto);
        given(notificationMapper.toDto(secondNotification))
                .willReturn(secondDto);

        List<NotificationDto> actual =
                notificationService
                        .findAllByReceiverId(receiverId);

        assertThat(actual)
                .containsExactly(firstDto, secondDto);
    }

    @Test
    void delete_본인의_알림을_삭제한다() {
        UUID notificationId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User receiver = new User(
                "receiver",
                "receiver@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(
                receiver,
                "id",
                requesterId
        );

        Notification notification =
                new Notification(
                        receiver,
                        "알림",
                        "알림 내용"
                );

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        notificationService.delete(
                notificationId,
                requesterId
        );

        then(notificationRepository)
                .should()
                .delete(notification);
    }

    @Test
    void delete_다른_사용자의_알림이면_실패한다() {
        UUID notificationId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User receiver = new User(
                "receiver",
                "receiver@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(
                receiver,
                "id",
                receiverId
        );

        Notification notification =
                new Notification(
                        receiver,
                        "알림",
                        "알림 내용"
                );

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.of(notification));

        assertThatThrownBy(
                () -> notificationService.delete(
                        notificationId,
                        requesterId
                )
        )
                .isInstanceOf(
                        NotificationAccessDeniedException.class
                )
                .hasMessage(
                        "본인의 알림만 확인할 수 있습니다."
                );

        then(notificationRepository)
                .should(never())
                .delete(any(Notification.class));
    }

    @Test
    void delete_알림이_없으면_실패한다() {
        UUID notificationId = UUID.randomUUID();

        given(notificationRepository.findById(notificationId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> notificationService.delete(
                        notificationId,
                        UUID.randomUUID()
                )
        )
                .isInstanceOf(
                        NotificationNotFoundException.class
                )
                .hasMessage("알림을 찾을 수 없습니다.");

        then(notificationRepository)
                .should(never())
                .delete(any(Notification.class));
    }
}