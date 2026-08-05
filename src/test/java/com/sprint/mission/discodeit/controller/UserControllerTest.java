package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {
        UserController.class,
        GlobalExceptionHandler.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("유효한 요청으로 사용자를 생성하면 200 응답을 반환한다")
    void createSuccess() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "user01",
                "user01@example.com",
                null,
                false
        );

        given(userService.create(any(CreateUserRequest.class)))
                .willReturn(response);

        String requestJson = """
                {
                  "username": "user01",
                  "email": "user01@example.com",
                  "password": "password123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("user01"))
                .andExpect(jsonPath("$.email").value("user01@example.com"))
                .andExpect(jsonPath("$.profile").doesNotExist())
                .andExpect(jsonPath("$.online").value(false));

        then(userService)
                .should()
                .create(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("사용자 생성 요청의 이메일 형식이 잘못되면 400 응답을 반환한다")
    void createFailWhenEmailIsInvalid() throws Exception {
        // given
        String requestJson = """
                {
                  "username": "user01",
                  "email": "invalid-email",
                  "password": "password123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.details.email")
                        .value("올바른 이메일 형식이어야 합니다."));

        then(userService)
                .should(never())
                .create(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 404 응답을 반환한다")
    void findFailWhenUserNotFound() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        given(userService.find(userId))
                .willThrow(new UserNotFoundException(userId));

        // when & then
        mockMvc.perform(
                        get("/users/{id}", userId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.exceptionType")
                        .value("UserNotFoundException"))
                .andExpect(jsonPath("$.details.userId")
                        .value(userId.toString()));

        then(userService)
                .should()
                .find(userId);
    }

    @Test
    @DisplayName("유효한 요청으로 사용자를 수정하면 변경된 사용자 정보를 반환한다")
    void updateSuccess() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "updated-user",
                "updated@example.com",
                null,
                true
        );

        given(userService.update(
                eq(userId),
                any(UpdateUserRequest.class)
        )).willReturn(response);

        String requestJson = """
                {
                  "username": "updated-user",
                  "email": "updated@example.com",
                  "password": "updatedPassword123"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("updated-user"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.online").value(true));

        then(userService)
                .should()
                .update(
                        eq(userId),
                        any(UpdateUserRequest.class)
                );
    }
}