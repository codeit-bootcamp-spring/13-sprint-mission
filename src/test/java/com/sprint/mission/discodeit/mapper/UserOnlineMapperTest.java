package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 온라인 상태 매퍼 단위 테스트")
class UserOnlineMapperTest {

    @InjectMocks
    UserOnlineMapper userOnlineMapper;

    @Mock
    SessionRegistry sessionRegistry;

    @Test
    @DisplayName("사용자의 활성 세션이 있으면 온라인으로 판단한다")
    void isOnline_returnsTrue_whenUserHasActiveSession() {
        UUID userId = UUID.randomUUID();
        DiscodeitUserDetails principal = principal(userId);
        SessionInformation session = new SessionInformation(principal, "session-id", new Date());

        given(sessionRegistry.getAllPrincipals()).willReturn(List.of(principal));
        given(sessionRegistry.getAllSessions(principal, false)).willReturn(List.of(session));

        assertThat(userOnlineMapper.isOnline(userId)).isTrue();
    }

    @Test
    @DisplayName("사용자의 활성 세션이 없으면 오프라인으로 판단한다")
    void isOnline_returnsFalse_whenUserHasNoActiveSession() {
        UUID userId = UUID.randomUUID();
        DiscodeitUserDetails principal = principal(userId);

        given(sessionRegistry.getAllPrincipals()).willReturn(List.of(principal));
        given(sessionRegistry.getAllSessions(principal, false)).willReturn(List.of());

        assertThat(userOnlineMapper.isOnline(userId)).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 활성 세션은 온라인 판단에 포함하지 않는다")
    void isOnline_returnsFalse_whenOnlyOtherUserHasActiveSession() {
        UUID userId = UUID.randomUUID();
        DiscodeitUserDetails otherPrincipal = principal(UUID.randomUUID());

        given(sessionRegistry.getAllPrincipals()).willReturn(List.of(otherPrincipal));

        assertThat(userOnlineMapper.isOnline(userId)).isFalse();
    }

    private DiscodeitUserDetails principal(UUID userId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        UserDto userDto = new UserDto(
                userId,
                "testUser",
                "test@example.com",
                null,
                true,
                Role.USER,
                now,
                now
        );
        return new DiscodeitUserDetails(userDto, "encodedPassword");
    }
}
