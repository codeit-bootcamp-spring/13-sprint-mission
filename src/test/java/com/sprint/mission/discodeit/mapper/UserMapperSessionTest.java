package com.sprint.mission.discodeit.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

class UserMapperSessionTest {

  private final SessionRegistryImpl registry = new SessionRegistryImpl();
  private final UserMapper mapper = new UserMapper(mock(BinaryContentMapper.class), registry);
  private final User user = User.builder().username("user").email("user@example.com")
      .password("password").role(Role.USER).build();

  @Test
  void onlineRequiresAnUnexpiredSessionForTheSameUser() {
    assertThat(mapper.toDto(user).online()).isFalse();
    DiscodeitUserDetails principal = new DiscodeitUserDetails(mapper.toDto(user), "password");
    registry.registerNewSession("first", principal);
    assertThat(mapper.toDto(user).online()).isTrue();

    User other = User.builder().username("other").email("other@example.com")
        .role(Role.USER).build();
    assertThat(mapper.toDto(other).online()).isFalse();

    registry.getSessionInformation("first").expireNow();
    assertThat(mapper.toDto(user).online()).isFalse();
    registry.registerNewSession("second", principal);
    assertThat(mapper.toDto(user).online()).isTrue();
    registry.removeSessionInformation("second");
    assertThat(mapper.toDto(user).online()).isFalse();
  }

  @Test
  void userIdentityStillMatchesAfterProfileChanges() {
    registry.registerNewSession("session",
        new DiscodeitUserDetails(mapper.toDto(user), "password"));
    user.updateEmail("changed@example.com");
    user.updateUserName("changed");
    assertThat(mapper.toDto(user).online()).isTrue();
  }

  @Test
  void sessionDestructionPublisherRemovesRegisteredSession() {
    MockServletContext servletContext = new MockServletContext();
    try (StaticWebApplicationContext context = new StaticWebApplicationContext()) {
      context.setServletContext(servletContext);
      context.addApplicationListener(registry);
      context.refresh();
      servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
          context);
      MockHttpSession session = new MockHttpSession(servletContext);
      registry.registerNewSession(session.getId(),
          new DiscodeitUserDetails(mapper.toDto(user), "password"));

      new SecurityConfig().httpSessionEventPublisher()
          .sessionDestroyed(new HttpSessionEvent(session));

      assertThat(registry.getSessionInformation(session.getId())).isNull();
      assertThat(mapper.toDto(user).online()).isFalse();
    }
  }
}
