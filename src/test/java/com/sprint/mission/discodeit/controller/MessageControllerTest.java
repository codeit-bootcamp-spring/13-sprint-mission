package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    private final UUID messageId = UUID.randomUUID();
    private final UUID channelId = UUID.randomUUID();
    private final UUID authorId = UUID.randomUUID();

    @Nested
    @DisplayName("메시지 생성")
    class CreateMessage {

        @Test
        @DisplayName("메시지 생성 성공")
        void 메시지_생성_성공() throws Exception {
            // given
            MessageCreateRequest request = new MessageCreateRequest(
                    "안녕하세요",
                    channelId,
                    authorId
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "messageCreateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            MessageDto response = new MessageDto(
                    messageId,
                    Instant.now(),
                    Instant.now(),
                    "안녕하세요",
                    channelId,
                    null,
                    List.of()
            );

            given(messageService.createMessage(
                    any(MessageCreateRequest.class),
                    isNull()
            )).willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/messages")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.content")
                            .value("안녕하세요"));

            then(messageService).should()
                    .createMessage(
                            any(MessageCreateRequest.class),
                            isNull()
                    );
        }

        @Test
        @DisplayName("메시지 내용이 비어 있으면 400을 반환")
        void 메시지_생성_실패_유효성_검증() throws Exception {
            // given
            MessageCreateRequest request = new MessageCreateRequest(
                    "",
                    channelId,
                    authorId
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "messageCreateRequest",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(multipart("/api/messages")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("채널별 메시지 조회")
    class FindAllByChannelId {

        @Test
        @DisplayName("채널의 메시지 목록 조회 성공")
        void 메시지_목록_조회_성공() throws Exception {
            // given
            MessageDto firstMessage = new MessageDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    Instant.now(),
                    "첫 번째 메시지",
                    channelId,
                    null,
                    List.of()
            );

            MessageDto secondMessage = new MessageDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    Instant.now(),
                    "두 번째 메시지",
                    channelId,
                    null,
                    List.of()
            );

            PageResponse<MessageDto> response = new PageResponse<>(
                    List.of(firstMessage, secondMessage),
                    null,
                    2,
                    false,
                    2L
            );

            given(messageService.getMessagesByChannelId(
                    eq(channelId),
                    isNull(),
                    any(Pageable.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/messages")
                                    .param("channelId", channelId.toString())
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("sort", "createdAt,desc")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].content")
                            .value("첫 번째 메시지"))
                    .andExpect(jsonPath("$.content[1].content")
                            .value("두 번째 메시지"))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false));

            then(messageService).should()
                    .getMessagesByChannelId(
                            eq(channelId),
                            isNull(),
                            any(Pageable.class)
                    );
        }

        @Test
        @DisplayName("channelId가 없으면 400을 반환")
        void 메시지_목록_조회_실패_channelId_없음() throws Exception {
            // when & then
            mockMvc.perform(get("/api/messages")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class UpdateMessage {

        @Test
        @DisplayName("메시지 수정 성공")
        void 메시지_수정_성공() throws Exception {
            // given
            MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지");

            MessageDto response = new MessageDto(
                    messageId,
                    Instant.now(),
                    Instant.now(),
                    "수정된 메시지",
                    channelId,
                    null,
                    List.of()
            );

            given(messageService.updateMessage(
                    eq(messageId),
                    any(MessageUpdateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.content")
                            .value("수정된 메시지"));

            then(messageService).should()
                    .updateMessage(
                            eq(messageId),
                            any(MessageUpdateRequest.class)
                    );
        }

        @Test
        @DisplayName("수정할 메시지 내용이 비어 있으면 400을 반환")
        void 메시지_수정_실패_유효성_검증() throws Exception {
            // given
            MessageUpdateRequest request = new MessageUpdateRequest("");

            // when & then
            mockMvc.perform(
                            patch("/api/messages/{messageId}", messageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class DeleteMessage {

        @Test
        @DisplayName("메시지 삭제 성공")
        void 메시지_삭제_성공() throws Exception {
            // given
            willDoNothing()
                    .given(messageService)
                    .deleteMessage(messageId);

            // when & then
            mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                    .andExpect(status().isNoContent());

            then(messageService).should()
                    .deleteMessage(messageId);
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 삭제하면 404를 반환")
        void 메시지_삭제_실패_메시지_없음() throws Exception {
            // given
            willThrow(new MessageNotFoundException(messageId))
                    .given(messageService)
                    .deleteMessage(messageId);

            // when & then
            mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("MESSAGE_NOT_FOUND"))
                    .andExpect(jsonPath("$.status")
                            .value(404))
                    .andExpect(jsonPath("$.details.messageId")
                            .value(messageId.toString()));

            then(messageService).should()
                    .deleteMessage(messageId);
        }
    }

}