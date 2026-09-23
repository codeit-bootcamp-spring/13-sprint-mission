package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserRoleQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 역할 조회 서비스 단위 테스트")
class UserRoleQueryServiceTest {

    @InjectMocks
    BasicUserRoleQueryService userRoleQueryService;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("해당 역할을 가진 사용자가 존재하면 true를 반환한다")
    void existsByRole_returnsTrue_whenRoleExists() {
        given(userRepository.existsByRole(Role.ADMIN)).willReturn(true);

        boolean result = userRoleQueryService.existsByRole(Role.ADMIN);

        assertThat(result).isTrue();
        then(userRepository).should().existsByRole(Role.ADMIN);
    }

    @Test
    @DisplayName("해당 역할을 가진 사용자가 없으면 false를 반환한다")
    void existsByRole_returnsFalse_whenRoleDoesNotExist() {
        given(userRepository.existsByRole(Role.CHANNEL_MANAGER)).willReturn(false);

        boolean result = userRoleQueryService.existsByRole(Role.CHANNEL_MANAGER);

        assertThat(result).isFalse();
        then(userRepository).should().existsByRole(Role.CHANNEL_MANAGER);
    }
}
