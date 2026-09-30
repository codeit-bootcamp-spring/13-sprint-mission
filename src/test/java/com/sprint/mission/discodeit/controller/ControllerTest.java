package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
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
        PageResponseMapper.class,
        SecurityConfig.class
})
@WithMockUser
@DisplayName("Controller Layer test.")
@Slf4j
public class ControllerTest {
    @Autowired
    MockMvc mockMvc;    // Mockup Controller

    @MockitoBean
    UserService userService;
    @MockitoBean
    MessageService messageService;
    @MockitoBean
    ChannelService channelService;

    @Mock
    MapStructMapper mapStructMapper;

    @MockitoBean
    PersistentTokenRepository persistentTokenRepository;
    @MockitoBean
    UserDetailsService userDetailsService;
    @MockitoBean
    SessionRegistry sessionRegistry;

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
