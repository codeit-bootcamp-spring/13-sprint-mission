package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "USER")
class UserApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;


  @Test
  @DisplayName("사용자 생성 API 통합 테스트")
  void createUser_Success() throws Exception {
    // Given
    UserCreateRequest createRequest = new UserCreateRequest(
        "testuser",
        "test@example.com",
        "Password1!"
    );

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(createRequest)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.username", is("testuser")))
        .andExpect(jsonPath("$.email", is("test@example.com")))
        .andExpect(jsonPath("$.profile.fileName", is("profile.jpg")))
        .andExpect(jsonPath("$.online", is(false)));
  }

  @Test
  @DisplayName("사용자 생성 실패 API 통합 테스트 - 유효하지 않은 요청")
  void createUser_Failure_InvalidRequest() throws Exception {
    // Given
    UserCreateRequest invalidRequest = new UserCreateRequest(
        "t", // 최소 길이 위반
        "invalid-email", // 이메일 형식 위반
        "short" // 비밀번호 정책 위반
    );

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("모든 사용자 조회 API 통합 테스트")
  void findAllUsers_Success() throws Exception {
    // Given
    // 테스트 사용자 생성 - Service를 통해 초기화
    UserCreateRequest userRequest1 = new UserCreateRequest(
        "user1",
        "user1@example.com",
        "Password1!"
    );

    UserCreateRequest userRequest2 = new UserCreateRequest(
        "user2",
        "user2@example.com",
        "Password1!"
    );

    userService.create(userRequest1, Optional.empty());
    userService.create(userRequest2, Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].username", is("user1")))
        .andExpect(jsonPath("$[0].email", is("user1@example.com")))
        .andExpect(jsonPath("$[1].username", is("user2")))
        .andExpect(jsonPath("$[1].email", is("user2@example.com")));
  }

  @Test
  @DisplayName("사용자 업데이트 API 통합 테스트")
  void updateUser_Success() throws Exception {
    // Given
    // 테스트 사용자 생성 - Service를 통해 초기화
    UserCreateRequest createRequest = new UserCreateRequest(
        "originaluser",
        "original@example.com",
        "Password1!"
    );

    UserDto createdUser = userService.create(createRequest, Optional.empty());
    UUID userId = createdUser.id();
    authenticateAs(createdUser, Role.USER);

    UserUpdateRequest updateRequest = new UserUpdateRequest(
        "updateduser",
        "updated@example.com",
        "UpdatedPassword1!"
    );

    MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(updateRequest)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "updated-profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "updated-image".getBytes()
    );

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(userUpdateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId.toString())))
        .andExpect(jsonPath("$.username", is("updateduser")))
        .andExpect(jsonPath("$.email", is("updated@example.com")))
        .andExpect(jsonPath("$.profile.fileName", is("updated-profile.jpg")));
  }

  @Test
  @DisplayName("사용자 업데이트 실패 API 통합 테스트 - 본인이 아닌 사용자")
  void updateUser_Forbidden_NotOwner() throws Exception {
    // Given
    UserDto owner = userService.create(
        new UserCreateRequest("owneruser", "owner@example.com", "Password1!"), Optional.empty());
    UserDto other = userService.create(
        new UserCreateRequest("otheruser", "other@example.com", "Password1!"), Optional.empty());

    // 다른 사용자로 인증한 뒤 owner의 정보를 수정 시도
    authenticateAs(other, Role.USER);

    UserUpdateRequest updateRequest = new UserUpdateRequest(
        "hackeduser",
        "hacked@example.com",
        "HackedPassword1!"
    );

    MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(updateRequest)
    );

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", owner.id())
            .file(userUpdateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("사용자 삭제 API 통합 테스트")
  void deleteUser_Success() throws Exception {
    // Given
    // 테스트 사용자 생성 - Service를 통해 초기화
    UserCreateRequest createRequest = new UserCreateRequest(
        "deleteuser",
        "delete@example.com",
        "Password1!"
    );

    UserDto createdUser = userService.create(createRequest, Optional.empty());
    UUID userId = createdUser.id();
    authenticateAs(createdUser, Role.USER);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", userId)
        .with(csrf()))
        .andExpect(status().isNoContent());

    // 삭제 확인
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '" + userId + "')]").doesNotExist());
  }

  @Test
  @DisplayName("사용자 삭제 실패 API 통합 테스트 - 본인이 아닌 사용자")
  void deleteUser_Forbidden_NotOwner() throws Exception {
    // Given
    UserDto owner = userService.create(
        new UserCreateRequest("deleteowner", "deleteowner@example.com", "Password1!"),
        Optional.empty());
    UserDto other = userService.create(
        new UserCreateRequest("deleteother", "deleteother@example.com", "Password1!"),
        Optional.empty());

    authenticateAs(other, Role.USER);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", owner.id())
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  /**
   * 소유권 기반 인가(@PreAuthorize)를 검증하려면 principal이 DiscodeitUserDetails여야 하므로
   * 실제 생성된 사용자 정보로 SecurityContext를 교체한다.
   */
  private void authenticateAs(UserDto user, Role role) {
    UserDto principalDto = new UserDto(
        user.id(), user.username(), user.email(), user.profile(), true, role);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(principalDto, "encoded-password");
    TestSecurityContextHolder.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()));
  }
}
