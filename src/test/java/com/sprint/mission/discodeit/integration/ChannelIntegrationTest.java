package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ChannelRepository channelRepository;

  @Test
  @Transactional
  @DisplayName("Public 채널 생성 API - 요청하면 DB에 저장되고 201을 반환한다")
  void createPublicChannel_success() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("통합테스트채널", "설명");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("통합테스트채널"));

    org.assertj.core.api.Assertions.assertThat(
        channelRepository.findAll().stream()
            .anyMatch(c -> c.getName().equals("통합테스트채널"))).isTrue();
  }

  @Test
  @Transactional
  @DisplayName("채널 수정 API - PUBLIC 채널을 수정하면 실제로 반영된다")
  void updateChannel_success() throws Exception {
    // given
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "기존이름", "기존설명"));
    ChannelUpdateRequest request = new ChannelUpdateRequest("새이름", "새설명");

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("새이름"));
  }

  @Test
  @Transactional
  @DisplayName("채널 삭제 API - 삭제 요청하면 실제로 DB에서 제거된다")
  void deleteChannel_success() throws Exception {
    // given
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "삭제할채널", "설명"));
    UUID channelId = channel.getId();

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(channelRepository.findById(channelId)).isEmpty();
  }
}