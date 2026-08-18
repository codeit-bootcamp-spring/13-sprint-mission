package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Test
  @DisplayName("정상적인 요청으로 사용자를 생성하면 201 응답을 반환한다")
  void 사용자_생성_성공() throws Exception {
    // given
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        """
            {
              "username": "testUser",
              "email": "test@test.com",
              "password": "password"
            }
            """.getBytes(StandardCharsets.UTF_8)
    );

    // 아직 UserDto 생성자 정보가 없으므로 Mock 객체를 사용한다.
    // 컨트롤러가 서비스 결과를 JSON 본문으로 반환하는지만 확인한다.
    UserDto response = mock(UserDto.class);

    given(
        userService.create(
            any(UserCreateRequest.class),
            isNull()
        )
    ).willReturn(response);

    // when & then
    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
        )
        .andExpect(status().isCreated())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            content().json(
                objectMapper.writeValueAsString(response)
            )
        );

    then(userService).should()
        .create(
            any(UserCreateRequest.class),
            isNull()
        );
  }

  @Test
  @DisplayName("잘못된 이메일로 사용자를 생성하면 400 응답을 반환한다")
  void 사용자_생성_실패_이메일_형식_오류() throws Exception {
    // given
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        """
            {
              "username": "testUser",
              "email": "invalid-email",
              "password": "password"
            }
            """.getBytes(StandardCharsets.UTF_8)
    );

    // when & then
    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
        )
        .andExpect(status().isBadRequest())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            jsonPath("$.code")
                .value("C002")
        )
        .andExpect(
            jsonPath("$.status")
                .value(400)
        )
        .andExpect(
            jsonPath("$.details.email")
                .exists()
        )
        .andExpect(
            jsonPath("$.exceptionType")
                .value("MethodArgumentNotValidException")
        );

    then(userService).shouldHaveNoInteractions();
  }
}