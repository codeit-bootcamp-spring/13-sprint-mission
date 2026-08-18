package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableJpaAuditing
@Transactional
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("사용자를 생성하면 데이터베이스에 저장된 사용자 정보를 반환한다")
  void 사용자_생성_성공() throws Exception {
    // given
    MockMultipartFile requestPart = createRequestPart(
        "testUser",
        "test@test.com",
        "password"
    );

    // when & then
    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.username").value("testUser"))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("생성된 사용자를 목록에서 조회할 수 있다")
  void 사용자_목록_조회_성공() throws Exception {
    // given
    UUID userId = createUser(
        "testUser",
        "test@test.com"
    );

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(userId.toString()))
        .andExpect(jsonPath("$[0].username").value("testUser"))
        .andExpect(jsonPath("$[0].email").value("test@test.com"));
  }

  @Test
  @DisplayName("생성된 사용자의 정보를 수정할 수 있다")
  void 사용자_수정_성공() throws Exception {
    // given
    UUID userId = createUser(
        "testUser",
        "test@test.com"
    );

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        """
            {
              "newUsername": "updatedUser",
              "newEmail": "updated@test.com",
              "newPassword": "updatedPassword"
            }
            """.getBytes(StandardCharsets.UTF_8)
    );

    // when & then
    mockMvc.perform(
            multipart("/api/users/{userId}", userId)
                .file(requestPart)
                .with(request -> {
                  request.setMethod("PATCH");
                  return request;
                })
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("updatedUser"))
        .andExpect(jsonPath("$.email").value("updated@test.com"));
  }

  @Test
  @DisplayName("생성된 사용자를 삭제하면 더 이상 조회할 수 없다")
  void 사용자_삭제_성공() throws Exception {
    // given
    UUID userId = createUser(
        "testUser",
        "test@test.com"
    );

    // when
    mockMvc.perform(
            delete("/api/users/{userId}", userId)
        )
        .andExpect(status().isNoContent());

    // then
    mockMvc.perform(
            get("/api/users/{userId}", userId)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("U001"))
        .andExpect(jsonPath("$.status").value(404));
  }

  private UUID createUser(
      String username,
      String email
  ) throws Exception {
    MockMultipartFile requestPart = createRequestPart(
        username,
        email,
        "password"
    );

    MvcResult result = mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
        )
        .andExpect(status().isCreated())
        .andReturn();

    JsonNode responseBody = objectMapper.readTree(
        result.getResponse().getContentAsString()
    );

    return UUID.fromString(
        responseBody.get("id").asText()
    );
  }

  private MockMultipartFile createRequestPart(
      String username,
      String email,
      String password
  ) {
    String requestBody = """
        {
          "username": "%s",
          "email": "%s",
          "password": "%s"
        }
        """.formatted(
        username,
        email,
        password
    );

    return new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        requestBody.getBytes(StandardCharsets.UTF_8)
    );
  }
}