package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;

    private final UUID channelId = UUID.randomUUID();

    @Nested
    @DisplayName("공개 채널 생성")
    class CreatePublicChannel {

        @Test
        @DisplayName("올바른 요청이면 공개 채널을 생성하고 201을 반환")
        void 공개_채널_생성_성공() throws Exception {
            // given
            PublicChannelCreateRequest request =
                    new PublicChannelCreateRequest(
                            "공지",
                            "전체 공지 채널"
                    );

            ChannelDto response = new ChannelDto(
                    channelId,
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널",
                    List.of(),
                    Instant.now()
            );

            given(channelService.createPublicChannel(any(PublicChannelCreateRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/channels/public")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("공지"));

            then(channelService).should()
                    .createPublicChannel(
                            any(PublicChannelCreateRequest.class)
                    );
        }

        @Test
        @DisplayName("채널 이름이 비어 있으면 400을 반환")
        void 공개_채널_생성_실패_유효성_검증() throws Exception {
            // given
            PublicChannelCreateRequest request =
                    new PublicChannelCreateRequest(
                            "",
                            "전체 공지 채널"
                    );

            // when & then
            mockMvc.perform(post("/api/channels/public")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("사용자별 채널 조회")
    class FindChannelsByUser {

        @Test
        @DisplayName("사용자가 볼 수 있는 채널 목록을 반환")
        void 채널_목록_조회_성공() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            ChannelDto firstChannel = new ChannelDto(
                    UUID.randomUUID(),
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널",
                    List.of(),
                    Instant.now()
            );

            ChannelDto secondChannel = new ChannelDto(
                    UUID.randomUUID(),
                    ChannelType.PUBLIC,
                    "자유",
                    "자유 대화 채널",
                    List.of(),
                    Instant.now()
            );

            given(channelService.getChannelsByUserId(userId))
                    .willReturn(List.of(firstChannel, secondChannel));

            // when & then
            mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("공지"))
                    .andExpect(jsonPath("$[1].name").value("자유"));

            then(channelService).should()
                    .getChannelsByUserId(userId);
        }

        @Test
        @DisplayName("userId가 없으면 400을 반환")
        void 채널_목록_조회_실패_요청값_없음() throws Exception {
            // when & then
            mockMvc.perform(get("/api/channels"))
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("공개 채널 수정")
    class UpdateChannel {

        @Test
        @DisplayName("공개 채널 정보를 수정하고 200을 반환")
        void 공개_채널_수정_성공() throws Exception {
            // given
            PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(
                            "수정된 공지",
                            "수정된 설명"
                    );

            ChannelDto response = new ChannelDto(
                    channelId,
                    ChannelType.PUBLIC,
                    "수정된 공지",
                    "수정된 설명",
                    List.of(),
                    Instant.now()
            );

            given(channelService.updateChannel(eq(channelId), any(PublicChannelUpdateRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.name")
                            .value("수정된 공지"));

            then(channelService).should()
                    .updateChannel(
                            eq(channelId),
                            any(PublicChannelUpdateRequest.class)
                    );
        }

        @Test
        @DisplayName("채널 이름이 비어 있으면 400을 반환")
        void 공개_채널_수정_실패_유효성_검증() throws Exception {
            // given
            PublicChannelUpdateRequest request =
                    new PublicChannelUpdateRequest(
                            "",
                            "수정된 설명"
                    );

            // when & then
            mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class DeleteChannel {

        @Test
        @DisplayName("채널 삭제에 성공하면 204를 반환")
        void 채널_삭제_성공() throws Exception {
            // given
            willDoNothing()
                    .given(channelService)
                    .deleteChannel(channelId);

            // when & then
            mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                    .andExpect(status().isNoContent());

            then(channelService).should()
                    .deleteChannel(channelId);
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 404를 반환")
        void 채널_삭제_실패_채널_없음() throws Exception {
            // given
            willThrow(new ChannelNotFoundException(channelId))
                    .given(channelService)
                    .deleteChannel(channelId);

            // when & then
            mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("CHANNEL_NOT_FOUND"))
                    .andExpect(jsonPath("$.status")
                            .value(404));

            then(channelService).should()
                    .deleteChannel(channelId);
        }
    }
}