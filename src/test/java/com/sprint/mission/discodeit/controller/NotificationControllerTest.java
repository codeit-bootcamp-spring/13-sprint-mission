package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void findAll_로그인한_사용자의_알림을_반환한다() {
        UUID userId = UUID.randomUUID();

        DiscodeitUserDetails userDetails =
                userDetails(userId);

        List<NotificationDto> notifications = List.of(
                new NotificationDto(
                        UUID.randomUUID(),
                        Instant.now(),
                        userId,
                        "첫 번째 알림",
                        "첫 번째 내용"
                ),
                new NotificationDto(
                        UUID.randomUUID(),
                        Instant.now(),
                        userId,
                        "두 번째 알림",
                        "두 번째 내용"
                )
        );

        given(
                notificationService.findAllByReceiverId(
                        userId
                )
        ).willReturn(notifications);

        ResponseEntity<List<NotificationDto>> response =
                notificationController.findAll(userDetails);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsExactlyElementsOf(notifications);

        then(notificationService)
                .should()
                .findAllByReceiverId(userId);
    }

    @Test
    void delete_로그인한_사용자의_알림을_삭제한다() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        DiscodeitUserDetails userDetails =
                userDetails(userId);

        ResponseEntity<Void> response =
                notificationController.delete(
                        notificationId,
                        userDetails
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        then(notificationService)
                .should()
                .delete(notificationId, userId);
    }

    private DiscodeitUserDetails userDetails(UUID userId) {
        UserResponse user = new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                true,
                null,
                UserRole.USER
        );

        return new DiscodeitUserDetails(
                user,
                "encoded-password"
        );
    }
}