package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  @Test
  @DisplayName("공개 채널을 생성하면 201 응답과 채널 정보를 반환한다")
  void 공개_채널_생성_성공() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    PublicChannelCreateRequest request =
        new PublicChannelCreateRequest(
            "테스트 채널",
            "테스트 채널입니다."
        );

    ChannelDto response = new ChannelDto(
        channelId,
        ChannelType.PUBLIC,
        request.name(),
        request.description(),
        List.of(),
        null
    );

    given(channelService.createPublicChannel(request))
        .willReturn(response);

    // when & then
    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            jsonPath("$.id")
                .value(channelId.toString())
        )
        .andExpect(
            jsonPath("$.type")
                .value("PUBLIC")
        )
        .andExpect(
            jsonPath("$.name")
                .value("테스트 채널")
        )
        .andExpect(
            jsonPath("$.description")
                .value("테스트 채널입니다.")
        )
        .andExpect(
            jsonPath("$.participants")
                .isArray()
        );

    then(channelService).should()
        .createPublicChannel(request);
  }

  @Test
  @DisplayName("존재하지 않는 채널을 조회하면 404 응답을 반환한다")
  void 채널_조회_실패_존재하지_않음() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    given(channelService.findById(channelId))
        .willThrow(new ChannelNotFoundException(channelId));

    // when & then
    mockMvc.perform(
            get("/api/channels/{channelId}", channelId)
        )
        .andExpect(status().isNotFound())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            jsonPath("$.code")
                .value("CH001")
        )
        .andExpect(
            jsonPath("$.status")
                .value(404)
        )
        .andExpect(
            jsonPath("$.exceptionType")
                .value("ChannelNotFoundException")
        );

    then(channelService).should()
        .findById(channelId);
  }
}