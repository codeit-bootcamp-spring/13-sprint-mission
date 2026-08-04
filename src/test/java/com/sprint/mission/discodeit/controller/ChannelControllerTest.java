package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.ErrorResponseHandler;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@Import({ErrorResponseHandler.class, ErrorCodeStatusMapper.class})
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널을 생성한다")
    void createPublic_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "general",
                "general channel"
        );
        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelService.createPublic(any(PublicChannelCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("general channel"));
    }

    @Test
    @DisplayName("PUBLIC 채널 생성 요청 값이 올바르지 않으면 400을 반환한다")
    void createPublic_fail_validation() throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "",
                "general channel"
        );

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @DisplayName("PRIVATE 채널을 생성한다")
    void createPrivate_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(userId1, userId2)
        );
        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PRIVATE,
                null,
                null,
                List.of(),
                null
        );

        given(channelService.createPrivate(any(PrivateChannelCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PRIVATE"));
    }

    @Test
    @DisplayName("사용자가 볼 수 있는 채널 목록을 조회한다")
    void findAllByUserId_success() throws Exception {
        UUID userId = UUID.randomUUID();
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(),
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelService.findAllByUserId(userId)).willReturn(List.of(channel));

        mockMvc.perform(get("/api/channels")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PUBLIC"))
                .andExpect(jsonPath("$[0].name").value("general"));
    }

    @Test
    @DisplayName("존재하지 않는 채널 조회 시 404를 반환한다")
    void find_fail_notFound() throws Exception {
        UUID channelId = UUID.randomUUID();

        given(channelService.findById(channelId)).willThrow(new ChannelNotFoundException(channelId));

        mockMvc.perform(get("/api/channels/{channelId}", channelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("채널을 수정한다")
    void update_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        ChannelUpdateRequest request = new ChannelUpdateRequest(
                channelId,
                "notice",
                "notice channel"
        );
        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "notice",
                "notice channel",
                List.of(),
                null
        );

        given(channelService.update(eq(channelId), any(ChannelUpdateRequest.class))).willReturn(response);

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.name").value("notice"))
                .andExpect(jsonPath("$.description").value("notice channel"));
    }

    @Test
    @DisplayName("채널을 삭제한다")
    void delete_success() throws Exception {
        UUID channelId = UUID.randomUUID();

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());

        then(channelService).should().delete(channelId);
    }
}