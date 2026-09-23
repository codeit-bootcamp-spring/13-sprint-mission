package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelPublicCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ChannelController 슬라이스 테스트")
class ChannelControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  ChannelService channelService;

  @Test
  @DisplayName("올바른 공개 채널 생성 요청이면 201과 채널 정보를 반환한다")
  void createPublicChannel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    ChannelPublicCreateRequest request =
        new ChannelPublicCreateRequest("공개채널", "공개 채널 입니다.");
    ChannelResponse response = new ChannelResponse(
        channelId, ChannelType.PUBLIC, "공개채널", "공개 채널 입니다.", List.of(), null);

    given(channelService.createPublicChannel(any(ChannelPublicCreateRequest.class)))
        .willReturn(response);

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(channelId.toString()))
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.name").value("공개채널"));
  }

  @Test
  @DisplayName("채널명이 비어 있으면 400과 검증 오류를 반환한다")
  void rejectBlankName() throws Exception {
    // given
    ChannelPublicCreateRequest invalidRequest =
        new ChannelPublicCreateRequest("", "공개 채널 입니다");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.details.fieldErrors").isArray());

    then(channelService).shouldHaveNoInteractions();
  }
}
