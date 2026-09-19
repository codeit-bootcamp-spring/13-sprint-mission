package com.sprint.mission.discodeit.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.controller.AuthController;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = GlobalExceptionHandler.class
    )
)
@Import({SecurityConfig.class, DiscodeitUserDetailsService.class})
class RememberMeIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private UserMapper userMapper;

  @MockitoBean
  private AuthenticationSuccessHandler loginSuccessHandler;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private UserService userService;

  @BeforeEach
  void setUp() {
    UserResponse admin = new UserResponse(
        UUID.randomUUID(), "admin", "admin@asdf.com", null, true, Role.ADMIN);
    User adminEntity = User.builder()
        .username(admin.username())
        .email(admin.email())
        .password(passwordEncoder.encode("admin1234"))
        .role(admin.role())
        .build();

    when(userRepository.findByUsername("admin")).thenReturn(java.util.Optional.of(adminEntity));
    when(userMapper.toDto(adminEntity)).thenReturn(admin);
    when(userService.findUserById(admin.id())).thenReturn(admin);
  }

  @Test
  @DisplayName("remember-me=true로 로그인하면 세션 없이도 쿠키로 다시 인증된다")
  void rememberMeCookieAuthenticatesAfterSessionIsLost() throws Exception {
    MvcResult loginResult = mvc.perform(post("/api/auth/login")
            .with(csrf())
            .param("username", "admin")
            .param("password", "admin1234")
            .param("remember-me", "true"))
        .andExpect(status().isOk())
        .andExpect(authenticated().withUsername("admin"))
        .andReturn();

    Cookie rememberMeCookie = loginResult.getResponse().getCookie("remember-me");
    assertThat(rememberMeCookie).isNotNull();
    assertThat(rememberMeCookie.getMaxAge()).isPositive();

    // 로그인 응답의 세션은 전달하지 않고 remember-me 쿠키만 보낸다.
    mvc.perform(get("/api/auth/me").cookie(rememberMeCookie))
        .andExpect(status().isOk())
        .andExpect(authenticated().withUsername("admin"))
        .andExpect(jsonPath("$.username").value("admin"));
  }
}
