package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserMapperImpl.class, BinaryContentMapperImpl.class, UserOnlineMapper.class})
@DisplayName("UserMapper 단위 테스트")
class UserMapperTest {

    @Autowired
    UserMapper userMapper;

    @MockitoBean
    SessionRegistry sessionRegistry;

    @Test
    @DisplayName("사용자 엔티티의 역할을 DTO에 매핑한다")
    void toDto_mapsUserRole() {
        User user = new User(
                new UserCreateCommand("testUser", "encodedPassword", "test@example.com"),
                null
        );
        user.updateRole(new UserRoleUpdateCommand(Role.CHANNEL_MANAGER));

        UserDto result = userMapper.toDto(user, true);

        assertThat(result.username()).isEqualTo(user.getUsername());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.online()).isTrue();
        assertThat(result.role()).isEqualTo(Role.CHANNEL_MANAGER);
    }
}
