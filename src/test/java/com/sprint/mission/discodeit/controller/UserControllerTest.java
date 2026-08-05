package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.ErrorResponseHandler;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({ErrorResponseHandler.class, ErrorCodeStatusMapper.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStatusService userStatusService;

    @Test
    @DisplayName("사용자를 생성한다")
    void createUser_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserCreateRequest request = new UserCreateRequest(
                "tester",
                "tester@example.com",
                "Password1!",
                null,
                null,
                null
        );
        UserDto response = new UserDto(userId, "tester", "tester@example.com", null, true);

        MockMultipartFile requestPart = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        given(userService.create(any(UserCreateRequest.class))).willReturn(response);

        mockMvc.perform(multipart("/api/users")
                        .file(requestPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.email").value("tester@example.com"))
                .andExpect(jsonPath("$.online").value(true));

        then(userService).should().create(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("사용자 생성 요청 값이 올바르지 않으면 400을 반환한다")
    void createUser_fail_validation() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "",
                "invalid-email",
                "123",
                null,
                null,
                null
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/users")
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.username").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    @DisplayName("사용자 목록을 조회한다")
    void findAll_success() throws Exception {
        UserDto user = new UserDto(UUID.randomUUID(), "tester", "tester@example.com", null, true);

        given(userService.findAll()).willReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("tester"))
                .andExpect(jsonPath("$[0].email").value("tester@example.com"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 404를 반환한다")
    void findById_fail_notFound() throws Exception {
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("사용자를 수정한다")
    void update_success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest(
                "updated",
                "updated@example.com",
                "NewPassword1!",
                null,
                null,
                null
        );
        UserDto response = new UserDto(userId, "updated", "updated@example.com", null, true);

        MockMultipartFile requestPart = new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        given(userService.update(eq(userId), any(UserUpdateRequest.class))).willReturn(response);

        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .file(requestPart)
                        .with(servletRequest -> {
                            servletRequest.setMethod("PATCH");
                            return servletRequest;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        ArgumentCaptor<UserUpdateRequest> captor = ArgumentCaptor.forClass(UserUpdateRequest.class);
        then(userService).should().update(eq(userId), captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("updated");
    }

    @Test
    @DisplayName("사용자를 삭제한다")
    void delete_success() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        then(userService).should().delete(userId);
    }

    @Test
    @DisplayName("사용자 상태를 수정한다")
    void updateStatus_success() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant lastSeenAt = Instant.now();
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(null, lastSeenAt);
        UserStatusDto response = new UserStatusDto(UUID.randomUUID(), userId, lastSeenAt, true);

        given(userStatusService.updateByUserId(eq(userId), any(UserStatusUpdateRequest.class))).willReturn(response);

        mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.isOnline").value(true));
    }
}