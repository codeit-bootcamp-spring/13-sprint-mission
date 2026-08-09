package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.command.CreateUserCommand;
import com.sprint.mission.discodeit.dto.command.UpdateUserCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStatusService userStatusService;

    @Test
    @DisplayName("사용자 생성에 성공하면 201과 생성된 사용자를 반환한다.")
    void create_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "홍길동",
                "hong12@test.com",
                null,
                false
        );

        given(userService.create(
                any(CreateUserCommand.class),
                isNull()
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", "hong12@test.com")
                                .param("password", "12345")
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("홍길동")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("hong12@test.com")
                )
                .andExpect(
                        jsonPath("$.profile")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.online")
                                .value(false)
                );

        ArgumentCaptor<CreateUserCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateUserCommand.class);

        then(userService)
                .should()
                .create(
                        commandCaptor.capture(),
                        isNull()
                );

        CreateUserCommand command = commandCaptor.getValue();

        assertThat(command.username())
                .isEqualTo("홍길동");

        assertThat(command.email())
                .isEqualTo("hong12@test.com");

        assertThat(command.password())
                .isEqualTo("12345");
    }

    @Test
    @DisplayName("사용자 이름이 공백이면 400을 반환한다")
    void create_fail_usernameBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", " ")
                                .param("email", "hong12@test.com")
                                .param("password", "12345")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.username")
                                .value("사용자 이름은 필수입니다.")
                );

        then(userService)
                .shouldHaveNoInteractions();

        then(userStatusService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이메일이 공백이면 400을 반환한다")
    void create_fail_emailBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", " ")
                                .param("password", "12345")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.email")
                                .value("올바른 이메일의 형식이 아닙니다.")
                );

        then(userService)
                .shouldHaveNoInteractions();

        then(userStatusService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
    void create_fail_emailInvalid() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", "false-email")
                                .param("password", "12345")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.email")
                                .exists()
                );

        then(userService)
                .shouldHaveNoInteractions();

        then(userStatusService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비밀번호가 공백이면 400을 반환한다")
    void create_fail_passwordBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", "hong12@test.com")
                                .param("password", " ")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.password")
                                .value("비밀번호는 5자 이상 20자 이하여야 합니다.")
                );

        then(userService)
                .shouldHaveNoInteractions();

        then(userStatusService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사용자 수정에 성공하면 200과 수정된 사용자를 반환한다")
    void update_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "홍감자",
                "hong12@test.com",
                null,
                false
        );

        given(userService.update(
                eq(userId),
                any(UpdateUserCommand.class),
                isNull()
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        multipart(
                                "/api/users/{userId}",
                                userId
                        )
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                                .param("username", "홍감자")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("홍감자")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("hong12@test.com")
                )
                .andExpect(
                        jsonPath("$.online")
                                .value(false)
                );

        ArgumentCaptor<UpdateUserCommand> commandCaptor =
                ArgumentCaptor.forClass(UpdateUserCommand.class);

        then(userService)
                .should()
                .update(
                        eq(userId),
                        commandCaptor.capture(),
                        isNull()
                );

        UpdateUserCommand command = commandCaptor.getValue();

        assertThat(command.username())
                .isEqualTo("홍감자");
        assertThat(command.email())
                .isNull();

        assertThat(command.password())
                .isNull();
    }

    @Test
    @DisplayName("이메일만 전달하면 이메일만 수정 요청으로 전달한다")
    void update_success_emailOnly() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "홍길동",
                "honghong12@test.com",
                null,
                false
        );

        given(userService.update(
                eq(userId),
                any(UpdateUserCommand.class),
                isNull()
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        multipart(
                                "/api/users/{userId}",
                                userId
                        )
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                                .param("email", "honghong12@test.com")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("홍길동")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("honghong12@test.com")
                );

        ArgumentCaptor<UpdateUserCommand> commandCaptor =
                ArgumentCaptor.forClass(UpdateUserCommand.class);

        then(userService)
                .should()
                .update(
                        eq(userId),
                        commandCaptor.capture(),
                        isNull()
                );

        UpdateUserCommand command = commandCaptor.getValue();

        assertThat(command.username())
                .isNull();

        assertThat(command.email())
                .isEqualTo("honghong12@test.com");

        assertThat(command.password())
                .isNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정하면 404를 반환한다")
    void update_fail_userNotFound() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        given(userService.update(
                eq(userId),
                any(UpdateUserCommand.class),
                isNull()
        )).willThrow(
                new UserNotFoundException(userId)
        );

        // when & then
        mockMvc.perform(
                        multipart(
                                "/api/users/{userId}",
                                userId
                        )
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                                .param("username", "홍감자")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("USER_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.userId")
                                .value(userId.toString())
                );

        then(userService)
                .should()
                .update(
                        eq(userId),
                        any(UpdateUserCommand.class),
                        isNull()
                );
    }

  }