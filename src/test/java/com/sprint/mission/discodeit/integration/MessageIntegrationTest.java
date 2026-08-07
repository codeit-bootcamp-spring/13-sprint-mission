package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("사용자와 채널이 존재하면 메시지를 생성할 수 있다")
  void 메시지_생성_성공() throws Exception {
    // given
    UUID userId = createUser();
    UUID channelId = createPublicChannel();

    MockMultipartFile requestPart = createMessageRequestPart(
        "테스트 메시지입니다.",
        channelId,
        userId
    );

    // when & then
    mockMvc.perform(
            multipart("/api/messages")
                .file(requestPart)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(
            jsonPath("$.content")
                .value("테스트 메시지입니다.")
        )
        .andExpect(
            jsonPath("$.channelId")
                .value(channelId.toString())
        )
        .andExpect(
            jsonPath("$.author.id")
                .value(userId.toString())
        );
  }

  @Test
  @DisplayName("생성된 메시지를 채널별 목록에서 조회할 수 있다")
  void 채널별_메시지_목록_조회_성공() throws Exception {
    // given
    UUID userId = createUser();
    UUID channelId = createPublicChannel();

    UUID messageId = createMessage(
        "목록 조회 메시지",
        channelId,
        userId
    );

    // when & then
    mockMvc.perform(
            get("/api/messages")
                .param("channelId", channelId.toString())
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(
            jsonPath("$.content[0].id")
                .value(messageId.toString())
        )
        .andExpect(
            jsonPath("$.content[0].content")
                .value("목록 조회 메시지")
        )
        .andExpect(jsonPath("$.number").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.totalElements").value(nullValue()));
  }

  @Test
  @DisplayName("생성된 메시지의 내용을 수정할 수 있다")
  void 메시지_수정_성공() throws Exception {
    // given
    UUID userId = createUser();
    UUID channelId = createPublicChannel();

    UUID messageId = createMessage(
        "수정 전 메시지",
        channelId,
        userId
    );

    String requestBody = """
        {
          "newContent": "수정된 메시지입니다."
        }
        """;

    // when & then
    mockMvc.perform(
            patch("/api/messages/{messageId}", messageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.id")
                .value(messageId.toString())
        )
        .andExpect(
            jsonPath("$.content")
                .value("수정된 메시지입니다.")
        );
  }

  @Test
  @DisplayName("생성된 메시지를 삭제하면 더 이상 조회할 수 없다")
  void 메시지_삭제_성공() throws Exception {
    // given
    UUID userId = createUser();
    UUID channelId = createPublicChannel();

    UUID messageId = createMessage(
        "삭제할 메시지",
        channelId,
        userId
    );

    // when
    mockMvc.perform(
            delete("/api/messages/{messageId}", messageId)
        )
        .andExpect(status().isNoContent());

    // then
    mockMvc.perform(
            get("/api/messages/{messageId}", messageId)
        )
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.code")
                .value("M001")
        )
        .andExpect(
            jsonPath("$.status")
                .value(404)
        );
  }

  private UUID createUser() throws Exception {
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        """
            {
              "username": "messageTestUser",
              "email": "message@test.com",
              "password": "password"
            }
            """.getBytes(StandardCharsets.UTF_8)
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

  private UUID createPublicChannel() throws Exception {
    String requestBody = """
        {
          "name": "메시지 테스트 채널",
          "description": "메시지 통합 테스트용 채널입니다."
        }
        """;

    MvcResult result = mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
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

  private UUID createMessage(
      String content,
      UUID channelId,
      UUID authorId
  ) throws Exception {
    MockMultipartFile requestPart = createMessageRequestPart(
        content,
        channelId,
        authorId
    );

    MvcResult result = mockMvc.perform(
            multipart("/api/messages")
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

  private MockMultipartFile createMessageRequestPart(
      String content,
      UUID channelId,
      UUID authorId
  ) {
    String requestBody = """
        {
          "content": "%s",
          "channelId": "%s",
          "authorId": "%s"
        }
        """.formatted(
        content,
        channelId,
        authorId
    );

    return new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        requestBody.getBytes(StandardCharsets.UTF_8)
    );
  }
}