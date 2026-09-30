package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.security.JwtRegistry;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 온라인 상태 매퍼 단위 테스트")
class UserOnlineMapperTest {

    @InjectMocks
    UserOnlineMapper userOnlineMapper;

    @Mock
    JwtRegistry jwtRegistry;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("해당 사용자 ID의 JWT 등록 여부로 온라인 상태를 판단한다")
    void isOnline_returnsRegistryStateForRequestedUser(boolean active) {
        UUID userId = UUID.randomUUID();
        given(jwtRegistry.hasActiveJwtInformationByUserId(userId)).willReturn(active);

        assertThat(userOnlineMapper.isOnline(userId)).isEqualTo(active);

        verify(jwtRegistry).hasActiveJwtInformationByUserId(userId);
    }
}
