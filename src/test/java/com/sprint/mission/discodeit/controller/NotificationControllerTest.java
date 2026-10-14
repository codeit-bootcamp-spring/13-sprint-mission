package com.sprint.mission.discodeit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Nested
    class GetNotifications {

        @Test
        void getNotifications_success() {
            // given
            UUID userId = UUID.randomUUID();

            UserDto userDto = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "password"
                    );

            List<NotificationDto> expected =
                    List.of();

            given(notificationService.getNotifications(userId))
                    .willReturn(expected);

            // when
            ResponseEntity<List<NotificationDto>> response =
                    notificationController.getNotifications(userDetails);

            // then
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody())
                    .isEqualTo(expected);

            then(notificationService).should()
                    .getNotifications(userId);
        }
    }

    @Nested
    class DeleteNotification {

        @Test
        void deleteNotification_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            UserDto userDto = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "password"
                    );

            // when
            ResponseEntity<Void> response =
                    notificationController.deleteNotification(
                            notificationId,
                            userDetails
                    );

            // then
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);

            then(notificationService).should()
                    .deleteNotification(
                            notificationId,
                            userId
                    );
        }
    }
}