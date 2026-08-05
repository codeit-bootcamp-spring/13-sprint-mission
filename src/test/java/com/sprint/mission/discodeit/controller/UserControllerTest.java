package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStatusService userStatusService;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("올바른 요청이면 사용자를 생성하고 201을 반환")
        void create_success() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UserDto response = new UserDto(
                    userId,
                    "user1",
                    "user1@test.com",
                    null,
                    true
            );

            // @RequestPart로 전달할 JSON 파트
            MockMultipartFile requestPart = new MockMultipartFile(
                    "userCreateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(userService.createUser(
                    any(UserCreateRequest.class),
                    isNull()
            )).willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/users").file(requestPart).contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(userId.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("user1"));

            then(userService).should()
                    .createUser(any(UserCreateRequest.class), isNull());
        }

        @Test
        @DisplayName("이메일 형식이 잘못되면 400을 반환")
        void create_fail() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "user1",
                    "잘못된 이메일",
                    "password1"
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "userCreateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(multipart("/api/users").file(requestPart).contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("전체 사용자 조회")
    class FindAll {

        @Test
        @DisplayName("전체 사용자 목록과 200을 반환")
        void get_success() throws Exception {
            // given
            UserDto firstUser = new UserDto(
                    UUID.randomUUID(),
                    "user1",
                    "user1@test.com",
                    null,
                    true
            );

            UserDto secondUser = new UserDto(
                    UUID.randomUUID(),
                    "user2",
                    "user2@test.com",
                    null,
                    false
            );

            given(userService.getUsers())
                    .willReturn(List.of(firstUser, secondUser));

            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].username")
                            .value("user1"))
                    .andExpect(jsonPath("$[1].username")
                            .value("user2"));

            then(userService).should().getUsers();
        }

        @Test
        @DisplayName("사용자가 없으면 빈 배열을 반환")
        void get_fail() throws Exception {
            // given
            given(userService.getUsers())
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            then(userService).should().getUsers();
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class UpdateUser {

        @Test
        @DisplayName("올바른 요청이면 사용자 정보를 수정하고 200을 반환")
        void update_success() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest(
                    "new-user",
                    "new-user@test.com",
                    "new-password"
            );

            UserDto response = new UserDto(
                    userId,
                    "new-user",
                    "new-user@test.com",
                    null,
                    true
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "userUpdateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(userService.updateUser(
                    eq(userId),
                    any(UserUpdateRequest.class),
                    isNull()
            )).willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}", userId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(userId.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("new-user"));

            then(userService).should()
                    .updateUser(
                            eq(userId),
                            any(UserUpdateRequest.class),
                            isNull()
                    );
        }

        @Test
        @DisplayName("수정할 사용자가 없으면 404를 반환")
        void update_fail() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest(
                    "new-user",
                    "new-user@test.com",
                    "new-password"
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "userUpdateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(userService.updateUser(
                    eq(userId),
                    any(UserUpdateRequest.class),
                    isNull()
            )).willThrow(new UserNotFoundException(userId));

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}", userId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.status")
                            .value(404))
                    .andExpect(jsonPath("$.message")
                            .value("존재하지 않는 유저입니다."))
                    .andExpect(jsonPath("$.details.userId")
                            .value(userId.toString()));
        }
    }

    @Nested
    @DisplayName("사용자 상태 수정")
    class UpdateUserStatus {

        @Test
        @DisplayName("사용자 온라인 상태를 수정하고 200을 반환")
        void update_success() throws Exception {
            // given
            UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());

            UserStatusDto response =
                    new UserStatusDto(
                            UUID.randomUUID(),
                            userId,
                            Instant.now()
                    );

            given(userStatusService.updateUserStatusByUserId(
                    eq(userId),
                    any(UserStatusUpdateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId")
                            .value(userId.toString()));

            then(userStatusService).should()
                    .updateUserStatusByUserId(
                            eq(userId),
                            any(UserStatusUpdateRequest.class)
                    );
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("사용자가 존재하면 삭제하고 204를 반환")
        void delete_success() throws Exception {
            // given
            // void 메서드는 별도 given 설정이 없어도 됨

            // when & then
            mockMvc.perform(delete("/api/users/{userId}", userId))
                    .andExpect(status().isNoContent());

            then(userService).should().deleteUser(userId);
        }

        @Test
        @DisplayName("삭제할 사용자가 없으면 404를 반환한다")
        void delete_fail() throws Exception {
            // given
            willThrow(new UserNotFoundException(userId))
                    .given(userService).deleteUser(userId);

            // when & then
            mockMvc.perform(delete("/api/users/{userId}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message")
                            .value("존재하지 않는 유저입니다."))
                    .andExpect(jsonPath("$.status")
                            .value(404));

            then(userService).should().deleteUser(userId);
        }
    }
}