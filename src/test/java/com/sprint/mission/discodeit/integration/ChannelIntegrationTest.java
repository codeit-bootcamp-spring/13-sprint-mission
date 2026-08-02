package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableJpaAuditing
@Transactional
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("공개 채널을 생성하면 데이터베이스에 저장된 채널 정보를 반환한다")
  void 공개_채널_생성_성공() throws Exception {
    // given
    String requestBody = """
        {
          "name": "테스트 채널",
          "description": "테스트 채널입니다."
        }
        """;

    // when & then
    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.name").value("테스트 채널"))
        .andExpect(
            jsonPath("$.description")
                .value("테스트 채널입니다.")
        );
  }

  @Test
  @DisplayName("생성된 공개 채널의 이름과 설명을 수정할 수 있다")
  void 공개_채널_수정_성공() throws Exception {
    // given
    UUID channelId = createPublicChannel();

    String requestBody = """
        {
          "newName": "수정된 채널",
          "newDescription": "수정된 설명입니다."
        }
        """;

    // when & then
    mockMvc.perform(
            patch("/api/channels/{channelId}", channelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.id")
                .value(channelId.toString())
        )
        .andExpect(
            jsonPath("$.name")
                .value("수정된 채널")
        )
        .andExpect(
            jsonPath("$.description")
                .value("수정된 설명입니다.")
        );
  }

  @Test
  @DisplayName("생성된 채널을 삭제하면 더 이상 조회할 수 없다")
  void 채널_삭제_성공() throws Exception {
    // given
    UUID channelId = createPublicChannel();

    // when
    mockMvc.perform(
            delete("/api/channels/{channelId}", channelId)
        )
        .andExpect(status().isNoContent());

    // then
    mockMvc.perform(
            get("/api/channels/{channelId}", channelId)
        )
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.code")
                .value("CHANNEL_NOT_FOUND")
        )
        .andExpect(
            jsonPath("$.status")
                .value(404)
        );
  }

  private UUID createPublicChannel() throws Exception {
    String requestBody = """
        {
          "name": "테스트 채널",
          "description": "테스트 채널입니다."
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
}