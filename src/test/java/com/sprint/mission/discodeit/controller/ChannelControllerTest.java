package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@Import(ErrorCodeStatusMapper.class)
@ActiveProfiles("test")
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;

    @Test
    void createPublicChannel_유효한_요청이면_201과_채널_JSON을_반환한다()
            throws Exception {
        UUID channelId = UUID.randomUUID();
        ChannelPublicRequest request =
                new ChannelPublicRequest("general", "일반 채널입니다.");
        ChannelResponse response = new ChannelResponse(
                channelId,
                "general",
                "일반 채널입니다.",
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelService.createPublicChannel(any(ChannelPublicRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("일반 채널입니다."))
                .andExpect(jsonPath("$.type").value("PUBLIC"));
    }

    @Test
    void updateChannel_채널이_없으면_404와_예외_JSON을_반환한다()
            throws Exception {
        UUID channelId = UUID.randomUUID();
        ChannelPublicRequest request =
                new ChannelPublicRequest("general", "일반 채널입니다.");

        given(channelService.update(eq(channelId), any(ChannelPublicRequest.class)))
                .willThrow(new ChannelNotFoundException(channelId));

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"))
                .andExpect(jsonPath("$.details.channelId").value(channelId.toString()));
    }
}