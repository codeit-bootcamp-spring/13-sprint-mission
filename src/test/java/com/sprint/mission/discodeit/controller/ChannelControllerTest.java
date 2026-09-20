package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@ContextConfiguration(classes = {
        ChannelController.class,
        GlobalExceptionHandler.class
})
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChannelService channelService;

    @Test
    @DisplayName("유효한 요청으로 채널을 생성하면 채널 정보를 반환한다")
    void createSuccess() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        Instant lastMessageAt = Instant.now();

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "일반 채널",
                "일반 대화를 위한 채널입니다.",
                List.of(),
                lastMessageAt
        );

        given(channelService.create(any(CreateChannelRequest.class)))
                .willReturn(response);

        String requestJson = """
                {
                  "name": "일반 채널",
                  "description": "일반 대화를 위한 채널입니다."
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/channels")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("일반 채널"))
                .andExpect(jsonPath("$.description")
                        .value("일반 대화를 위한 채널입니다."))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants").isEmpty())
                .andExpect(jsonPath("$.lastMessageAt").exists());

        then(channelService)
                .should()
                .create(any(CreateChannelRequest.class));
    }

    @Test
    @DisplayName("채널 이름이 비어 있으면 생성에 실패한다")
    void createFailWhenNameIsBlank() throws Exception {
        // given
        String requestJson = """
                {
                  "name": "",
                  "description": "설명입니다."
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/channels")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("요청 값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.details.name")
                        .value("채널 이름은 필수입니다."));

        then(channelService)
                .should(never())
                .create(any(CreateChannelRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 채널을 조회하면 404 응답을 반환한다")
    void findFailWhenChannelNotFound() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        given(channelService.find(channelId))
                .willThrow(new ChannelNotFoundException(channelId));

        // when & then
        mockMvc.perform(
                        get("/channels/{id}", channelId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("채널을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.exceptionType")
                        .value("ChannelNotFoundException"))
                .andExpect(jsonPath("$.details.channelId")
                        .value(channelId.toString()));

        then(channelService)
                .should()
                .find(channelId);
    }

    @Test
    @DisplayName("유효한 요청으로 채널을 수정하면 변경된 정보를 반환한다")
    void updateSuccess() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                "수정된 채널",
                "수정된 채널 설명입니다.",
                List.of(),
                null
        );

        given(channelService.update(
                eq(channelId),
                any(UpdateChannelRequest.class)
        )).willReturn(response);

        String requestJson = """
                {
                  "name": "수정된 채널",
                  "description": "수정된 채널 설명입니다."
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/channels/{id}", channelId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("수정된 채널"))
                .andExpect(jsonPath("$.description")
                        .value("수정된 채널 설명입니다."))
                .andExpect(jsonPath("$.participants").isArray());

        then(channelService)
                .should()
                .update(
                        eq(channelId),
                        any(UpdateChannelRequest.class)
                );
    }
}