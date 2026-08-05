package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.ErrorResponseHandler;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import({ErrorResponseHandler.class, ErrorCodeStatusMapper.class})
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @Test
    @DisplayName("메시지를 생성한다")
    void create_success() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                userId,
                List.of()
        );
        MessageDto response = new MessageDto(
                messageId,
                Instant.now(),
                null,
                "hello",
                channelId,
                null,
                List.of()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        given(messageService.create(any(MessageCreateRequest.class))).willReturn(response);

        mockMvc.perform(multipart("/api/messages")
                        .file(requestPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("메시지 생성 요청 값이 올바르지 않으면 400을 반환한다")
    void create_fail_validation() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                null,
                null,
                List.of()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/messages")
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.channelId").exists())
                .andExpect(jsonPath("$.details.userId").exists());
    }

    @Test
    @DisplayName("채널의 메시지 목록을 조회한다")
    void findAllByChannelId_success() throws Exception {
        UUID channelId = UUID.randomUUID();
        MessageDto message = new MessageDto(
                UUID.randomUUID(),
                Instant.now(),
                null,
                "hello",
                channelId,
                null,
                List.of()
        );
        PageResponse<MessageDto> response = new PageResponse<>(
                List.of(message),
                null,
                50,
                false,
                null
        );

        given(messageService.findAllByChannelId(eq(channelId), isNull(), any(Pageable.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 메시지 조회 시 404를 반환한다")
    void find_fail_notFound() throws Exception {
        UUID messageId = UUID.randomUUID();

        given(messageService.findById(messageId)).willThrow(new MessageNotFoundException(messageId));

        mockMvc.perform(get("/api/messages/{messageId}", messageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("메시지를 수정한다")
    void update_success() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "updated");
        MessageDto response = new MessageDto(
                messageId,
                Instant.now(),
                Instant.now(),
                "updated",
                channelId,
                null,
                List.of()
        );

        given(messageService.update(eq(messageId), any(MessageUpdateRequest.class))).willReturn(response);

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("updated"));
    }

    @Test
    @DisplayName("메시지 수정 요청 값이 올바르지 않으면 400을 반환한다")
    void update_fail_validation() throws Exception {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "");

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.content").exists());
    }

    @Test
    @DisplayName("메시지를 삭제한다")
    void delete_success() throws Exception {
        UUID messageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());

        then(messageService).should().delete(messageId);
    }
}