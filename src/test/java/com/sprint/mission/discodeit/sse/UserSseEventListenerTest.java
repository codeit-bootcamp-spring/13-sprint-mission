package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.UserSseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class UserSseEventListenerTest {

    @Test
    @DisplayName("사용자 생성 이벤트를 수신하면 UserDto를 브로드캐스트한다")
    void handleUserChanged_created() {
        // given
        SseService sseService = mock(SseService.class);

        UserSseEventListener listener =
                new UserSseEventListener(sseService);

        UUID userId = UUID.randomUUID();

        UserDto user = mock(UserDto.class);

        given(user.id()).willReturn(userId);

        UserSseEvent event = new UserSseEvent(
                "users.created",
                user
        );

        // when
        listener.handleUserChanged(event);

        // then
        then(sseService).should()
                .broadcast("users.created", user);
    }

    @Test
    @DisplayName("사용자 수정 이벤트를 수신하면 SSE로 브로드캐스트한다")
    void handleUserChanged_updated() {
        // given
        SseService sseService = mock(SseService.class);

        UserSseEventListener listener =
                new UserSseEventListener(sseService);

        UUID userId = UUID.randomUUID();

        UserDto user = mock(UserDto.class);
        given(user.id()).willReturn(userId);

        UserSseEvent event = new UserSseEvent(
                "users.updated",
                user
        );

        // when
        listener.handleUserChanged(event);

        // then
        then(sseService).should()
                .broadcast("users.updated", user);
    }
    
    @Test
    @DisplayName("사용자 삭제 이벤트를 수신하면 SSE로 브로드캐스트한다")
    void handleUserChanged_deleted() {
        // given
        SseService sseService = mock(SseService.class);

        UserSseEventListener listener =
                new UserSseEventListener(sseService);

        UUID userId = UUID.randomUUID();

        UserDto user = mock(UserDto.class);
        given(user.id()).willReturn(userId);

        UserSseEvent event = new UserSseEvent(
                "users.deleted",
                user
        );

        // when
        listener.handleUserChanged(event);

        // then
        then(sseService).should()
                .broadcast("users.deleted", user);
    }

}
