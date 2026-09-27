package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.service.basic.ChannelReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelGuard 단위 테스트")
class ChannelGuardTest {
    @Mock
    ChannelReader channelReader;

    @InjectMocks
    ChannelGuard channelGuard;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("대상 채널과 인증 사용자로 조회한 접근 결과를 반환한다")
    void access_checksRequestedChannelAndUser(boolean accessible) {
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(channelReader.isChannelAccessible(channelId, userId)).willReturn(accessible);

        assertThat(channelGuard.isAccess(channelId, userId)).isEqualTo(accessible);
        verify(channelReader).isChannelAccessible(channelId, userId);
    }

    @Test
    @DisplayName("채널 또는 사용자 ID가 없으면 조회 없이 거부한다")
    void access_rejectsMissingIds() {
        assertThat(channelGuard.isAccess(null, UUID.randomUUID())).isFalse();
        assertThat(channelGuard.isAccess(UUID.randomUUID(), null)).isFalse();
        verifyNoInteractions(channelReader);
    }
}
