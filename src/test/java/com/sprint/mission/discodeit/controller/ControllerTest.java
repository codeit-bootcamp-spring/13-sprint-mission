package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.config.DiscodeitConfig;
import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.filter.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.security.AuthChecker;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.jwt.*;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest({
        UserController.class,
        MessageController.class,
        ChannelController.class,
})
@Import({
        DiscodeitConfig.class,
        SecurityConfig.class,

        PageResponseMapper.class,

        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        JwtSuccessHandler.class,
        LoginFailureHandler.class,
        JwtLogoutHandler.class
})
@WithMockUser
@DisplayName("Controller Layer test.")
@Slf4j
@ActiveProfiles("test")
public class ControllerTest {
    @Autowired
    MockMvc mockMvc;    // Mockup Controller

    @MockitoBean
    UserService userService;
    @MockitoBean
    MessageService messageService;
    @MockitoBean
    ChannelService channelService;

    @MockitoBean
    PersistentTokenRepository persistentTokenRepository;
    @MockitoBean
    DiscodeitUserDetailsService userDetailsService;
    @MockitoBean
    JwtRegistry registry;

    @MockitoBean
    SessionRegistry sessionRegistry;

    @MockitoBean(name = "AuthChecker")
    AuthChecker authChecker;

    @Nested
    @DisplayName("user controller")
    class UserControllerTests {
        private User user = new User("김숙희","ksk@email.com","password",null, Role.USER);



        private UserDto getDto(User user){
            return new UserDto(null, user.getUsername(), user.getEmail(),null,false,Role.USER);
        }

        @Test
        @DisplayName("user create req")
        void create() throws Exception {
            // given
            MockMultipartFile request = new MockMultipartFile(
                    "userCreateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    """
                    {"username" : "김숙희","email":"ksk@email.com","password":"password"}
                    """.getBytes()
            );

            given(userService.create(any(UserCreateRequest.class), any(Optional.class)))
                    .willReturn(getDto(user));
            // when
            // then

            mockMvc.perform(multipart("/api/users").file(request).with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.username").value(user.getUsername()));

        }


        @Test
        @DisplayName("update")
        void update() throws Exception {
            // given
            MockMultipartFile request = new MockMultipartFile(
                    "userUpdateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    """
                    {"newUsername" : "김숙희","newEmail":"ksk@email.com","newPassword":"password"}
                    """.getBytes()
            );

            // when
            given(userService.update(any(UUID.class),any(UserUpdateRequest.class), any(Optional.class)))
                    .willReturn(getDto(user));

            // 목업 유저이므로 UserDetail 이 기본형태. 때문에 편의상 인가 검사는 이 매서드 값으로 조정.
            given(authChecker.isSameUser(any(UUID.class),any(String.class))).willReturn(true);

            // then
            mockMvc.perform(multipart("/api/users/" + UUID.randomUUID())
                            .file(request)
                            .with(s -> {
                                s.setMethod("PATCH");
                                return s;
                            })
                            .accept(MediaType.APPLICATION_JSON).with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.username").value(user.getUsername()));


        }
    }


}
