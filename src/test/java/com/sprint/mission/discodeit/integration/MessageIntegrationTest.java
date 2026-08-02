package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
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
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Test
  @Transactional
  @DisplayName("메시지 생성 API - 요청하면 DB에 저장되고 201을 반환한다")
  void createMessage_success() throws Exception {
    // given
    User author = userRepository.save(
        new User("msgAuthor", "password1234!", "msgauthor@example.com"));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "메시지채널", "설명"));
    MessageCreateRequest request = new MessageCreateRequest(
        "통합테스트 메시지", channel.getId(), author.getId(), List.of());

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("통합테스트 메시지"));
  }

  @Test
  @Transactional
  @DisplayName("메시지 수정 API - 요청하면 실제로 반영된다")
  void updateMessage_success() throws Exception {
    // given
    User author = userRepository.save(
        new User("editAuthor", "password1234!", "editauthor@example.com"));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", "설명"));
    Message message = messageRepository.save(new Message("원본내용", author, channel));
    MessageUpdateRequest request = new MessageUpdateRequest("수정된내용");

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", message.getId())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된내용"));
  }

  @Test
  @Transactional
  @DisplayName("메시지 삭제 API - 삭제 요청하면 실제로 DB에서 제거된다")
  void deleteMessage_success() throws Exception {
    // given
    User author = userRepository.save(
        new User("delAuthor", "password1234!", "delauthor@example.com"));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널2", "설명"));
    Message message = messageRepository.save(new Message("삭제될메시지", author, channel));
    UUID messageId = message.getId();

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(messageRepository.findById(messageId)).isEmpty();
  }

  @Test
  @Transactional
  @DisplayName("채널 메시지 목록 조회 API - 저장된 메시지를 조회한다")
  void findAllByChannelId_success() throws Exception {
    // given
    User author = userRepository.save(
        new User("listAuthor", "password1234!", "listauthor@example.com"));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널3", "설명"));
    messageRepository.save(new Message("메시지1", author, channel));

    // when & then
    mockMvc.perform(get("/api/messages").param("channelId", channel.getId().toString()))
        .andExpect(status().isOk());
  }
}