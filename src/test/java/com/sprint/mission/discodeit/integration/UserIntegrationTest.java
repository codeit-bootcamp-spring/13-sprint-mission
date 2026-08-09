package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  @Transactional
  @DisplayName("사용자 생성 API - 요청하면 실제로 DB에 저장되고 201을 반환한다")
  void createUser_success() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "integrationUser", "integration@example.com", "password1234!", null);
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("integrationUser"));

    // DB에 실제 반영됐는지 확인
    org.assertj.core.api.Assertions.assertThat(
        userRepository.findByUserName("integrationUser")).isPresent();
  }

  @Test
  @Transactional
  @DisplayName("사용자 목록 조회 API - 저장된 사용자를 조회한다")
  void findAllUsers_success() throws Exception {
    // given
    userRepository.save(new User("listUser", "password1234!", "listuser@example.com"));

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk());
  }

  @Test
  @Transactional
  @DisplayName("사용자 삭제 API - 삭제 요청하면 실제로 DB에서 제거된다")
  void deleteUser_success() throws Exception {
    // given
    User user = userRepository.save(
        new User("deleteUser", "password1234!", "deleteuser@example.com"));
    UUID userId = user.getId();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(userRepository.findById(userId)).isEmpty();
  }
}