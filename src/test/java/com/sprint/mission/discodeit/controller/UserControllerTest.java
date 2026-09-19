package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  UserService userService;


  @Test
  @DisplayName("올바른 사용자 생성 요청이면 201과 사용자 정보를 반환한다")
  void 사용자_생성_요청_201() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UserCreateRequest request = new UserCreateRequest(
        "password", "김김김", "asdf@test.com", null, Role.USER);
    UserResponse response = new UserResponse(
        userId, "김김김", "asdf@test.com", null, false, Role.USER);
    MockMultipartFile requestPart = 제이슨_파트_생성("userCreateRequest", request);

    given(userService.createUser(any(UserCreateRequest.class), isNull()))
        .willReturn(response);

    // when & then
    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("김김김"))
        .andExpect(jsonPath("$.email").value("asdf@test.com"));
  }

  @Test
  @DisplayName("사용자 생성값 null 400 과 검증 오류")
  void 유저생성오류_400() throws Exception {
    // given
    UserCreateRequest invalidRequest = new UserCreateRequest(
        "", "", "email", null, Role.USER);
    MockMultipartFile requestPart =
        제이슨_파트_생성("userCreateRequest", invalidRequest);

    // when & then
    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.details.fieldErrors").isArray());

    then(userService).shouldHaveNoInteractions();
  }

  private MockMultipartFile 제이슨_파트_생성(String name, Object value) throws Exception {
    return new MockMultipartFile(
        name,
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(value));
  }
}
