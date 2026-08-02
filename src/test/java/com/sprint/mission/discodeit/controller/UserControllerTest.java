package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.user.DuplicateUsernameException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({ErrorCodeStatusMapper.class})
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;
  @MockitoBean
  private UserStatusService userStatusService;
  @MockitoBean
  private BinaryContentService binaryContentService;

  @Test
  @DisplayName("유저 생성 성공 - 201과 생성된 UserDto를 반환한다")
  void create_success() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("duplicateUser", "test@example.com",
        "password1234!", null);
    UUID userId = UUID.randomUUID();
    UserDto responseDto = new UserDto(userId, "testuser", "test@example.com", null, false);

    given(userService.create(anyString(), anyString(), anyString(), any()))
        .willReturn(responseDto);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  @DisplayName("유저 생성 실패 - username 중복이면 409를 반환한다")
  void create_fail_duplicateUsername() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("duplicateUser", "test@example.com",
        "password1234!", null);

    given(userService.create(anyString(), anyString(), anyString(), any()))
        .willThrow(new DuplicateUsernameException("duplicateUser"));

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_USERNAME"));
  }
}