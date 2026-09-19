package com.sprint.mission.discodeit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelPublicCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Role;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Discodeit API 통합 테스트")
class DiscodeitApiIntegrationTest {

  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper objectMapper;

  @Test
  @DisplayName("사용자, 채널, 메시지와 Actuator API가 정상 동작한다")
  void 주요_API가_정상적으로_동작() throws Exception {
    UUID userId = 사용자_생성();

    MvcResult channelResult = mvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(
                new ChannelPublicCreateRequest("공개 채널", "공개채널입니다."))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andReturn();
    UUID channelId = 응답_식별자_추출(channelResult);

    MessageCreateRequest messageRequest =
        new MessageCreateRequest("안녕하세요", userId, channelId);
    mvc.perform(multipart("/api/messages")
            .file(제이슨_파트_생성("messageCreateRequest", messageRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("안녕하세요"));

    mvc.perform(get("/actuator/info"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.app.name").value("Discodeit"));
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  @DisplayName("잘못된 사용자, 채널, 메시지 요청은 400을 반환한다")
  void 잘못된_API_요청은_400을_반환() throws Exception {
    UserCreateRequest invalidUser =
        new UserCreateRequest("", "", "invalid", null, Role.USER);
    mvc.perform(multipart("/api/users")
            .file(제이슨_파트_생성("userCreateRequest", invalidUser)))
        .andExpect(status().isBadRequest());

    mvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(
                new ChannelPublicCreateRequest("", "talk"))))
        .andExpect(status().isBadRequest());

    MessageCreateRequest invalidMessage =
        new MessageCreateRequest("", UUID.randomUUID(), UUID.randomUUID());
    mvc.perform(multipart("/api/messages")
            .file(제이슨_파트_생성("messageCreateRequest", invalidMessage)))
        .andExpect(status().isBadRequest());
  }

  private UUID 사용자_생성() throws Exception {
    UserCreateRequest request = new UserCreateRequest(
        "password", "김김김", "asdf@test.com", null, Role.USER);
    MvcResult result = mvc.perform(multipart("/api/users")
            .file(제이슨_파트_생성("userCreateRequest", request)))
        .andExpect(status().isCreated())
        .andReturn();
    return 응답_식별자_추출(result);
  }

  private MockMultipartFile 제이슨_파트_생성(String name, Object value) throws Exception {
    return new MockMultipartFile(
        name, "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(value));
  }

  private UUID 응답_식별자_추출(MvcResult result) throws Exception {
    return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsByteArray())
        .get("id")
        .asText());
  }
}
