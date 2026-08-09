package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.CreatePublicChannelCommand;
import com.sprint.mission.discodeit.dto.command.UpdateChannelCommand;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequset;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@Import(GlobalExceptionHandler.class)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;

    @Test
    @DisplayName("공개 채널 생성에 성공하면 201과 생성된 채널을 반환한다")
    void createPublicChannel_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        "공지 채널",
                        "전체 공지 채널입니다."
                );

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "공지 채널",
                "전체 공지 채널입니다.",
                null,
                createdAt
        );

        given(channelService.createPublicChannel(
                any(CreatePublicChannelCommand.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
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
                                .value("공지 채널")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("전체 공지 채널입니다.")
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists()
                );

        ArgumentCaptor<CreatePublicChannelCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        CreatePublicChannelCommand.class
                );

        then(channelService)
                .should()
                .createPublicChannel(
                        commandCaptor.capture()
                );

        CreatePublicChannelCommand capturedCommand =
                commandCaptor.getValue();

        assertThat(capturedCommand.name())
                .isEqualTo("공지 채널");

        assertThat(capturedCommand.description())
                .isEqualTo("전체 공지 채널입니다.");
    }

    @Test
    @DisplayName("공개 채널 이름이 공백이면 400을 반환한다")
    void createPublicChannel_fail_nameBlank() throws Exception {
        // given
        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        " ",
                        "채널 이름 공백"
                );

        // when & then
        mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.name")
                                .value("채널 이름은 필수입니다.")
                );

        then(channelService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("공개 채널 이름이 null이면 400을 반환한다")
    void createPublicChannel_fail_nameNull() throws Exception {
        // given
        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        null,
                        "채널 이름 없음"
                );

        // when & then
        mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.name")
                                .value("채널 이름은 필수입니다.")
                );

        then(channelService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("채널 수정에 성공하면 200과 수정된 채널을 반환한다")
    void update_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "일반 공지 채널",
                        "공지만 전달하는 채넗입니다."
                );

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "일반 공지 채널",
                "공지만 전달하는 채넗입니다.",
                null,
                createdAt
        );

        given(channelService.update(
                eq(channelId),
                any(UpdateChannelCommand.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                channelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
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
                                .value("일반 공지 채널")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("공지만 전달하는 채넗입니다.")
                );

        ArgumentCaptor<UpdateChannelCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        UpdateChannelCommand.class
                );

        then(channelService)
                .should()
                .update(
                        eq(channelId),
                        commandCaptor.capture()
                );

        UpdateChannelCommand capturedCommand =
                commandCaptor.getValue();

        assertThat(capturedCommand.name())
                .isEqualTo("일반 공지 채널");

        assertThat(capturedCommand.description())
                .isEqualTo("공지만 전달하는 채넗입니다.");
    }

    @Test
    @DisplayName("채널 이름만 전달하면 이름만 수정 요청으로 전달한다")
    void update_success_nameOnly() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "일반 공지 채널",
                        null
                );

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "일반 공지 채널",
                "공지 채널입니다.",
                null,
                Instant.now()
        );

        given(channelService.update(
                eq(channelId),
                any(UpdateChannelCommand.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                channelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("일반 공지 채널")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("공지 채널입니다.")
                );

        ArgumentCaptor<UpdateChannelCommand> commandCaptor =
                ArgumentCaptor.forClass(
                        UpdateChannelCommand.class
                );

        then(channelService)
                .should()
                .update(
                        eq(channelId),
                        commandCaptor.capture()
                );

        UpdateChannelCommand capturedCommand =
                commandCaptor.getValue();

        assertThat(capturedCommand.name())
                .isEqualTo("일반 공지 채널");

        assertThat(capturedCommand.description())
                .isNull();
    }

    @Test
    @DisplayName("존재하지 않는 채널을 수정하면 404 에러 JSON을 반환한다")
    void update_fail_channelNotFound() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "일반 공지 채널",
                        null
                );

        given(channelService.update(
                eq(channelId),
                any(UpdateChannelCommand.class)
        )).willThrow(
                new ChannelNotFoundException(channelId)
        );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                channelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("CHANNEL_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.channelId")
                                .value(channelId.toString())
                );

        then(channelService)
                .should()
                .update(
                        eq(channelId),
                        any(UpdateChannelCommand.class)
                );
    }

    @Test
    @DisplayName("채널 ID가 UUID 형식이 아니면 400을 반환한다")
    void update_fail_invalidChannelId() throws Exception {
        // given
        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "일반 공지 채널",
                        null
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                "invalid-channel-id"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        then(channelService)
                .shouldHaveNoInteractions();
    }
}