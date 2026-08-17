package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
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

@WebMvcTest(MessageController.class)
@ContextConfiguration(classes = {
        MessageController.class,
        GlobalExceptionHandler.class
})
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    @DisplayName("유효한 요청으로 메시지를 생성하면 메시지 정보를 반환한다")
    void createSuccess() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserDto author = new UserDto(
                userId,
                "user01",
                "user01@example.com",
                null,
                true
        );

        MessageDto response = new MessageDto(
                messageId,
                Instant.now(),
                Instant.now(),
                "안녕하세요.",
                channelId,
                author,
                List.of()
        );

        given(messageService.create(any(CreateMessageRequest.class)))
                .willReturn(response);

        String requestJson = """
                {
                  "content": "안녕하세요.",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                userId,
                channelId
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("안녕하세요."))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(userId.toString()))
                .andExpect(jsonPath("$.author.username").value("user01"))
                .andExpect(jsonPath("$.attachments").isArray())
                .andExpect(jsonPath("$.attachments").isEmpty());

        then(messageService)
                .should()
                .create(any(CreateMessageRequest.class));
    }

    @Test
    @DisplayName("메시지 내용이 비어 있으면 생성에 실패한다")
    void createFailWhenContentIsBlank() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String requestJson = """
                {
                  "content": "",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                userId,
                channelId
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
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
                .andExpect(jsonPath("$.details.content")
                        .value("메시지 내용은 필수입니다."));

        then(messageService)
                .should(never())
                .create(any(CreateMessageRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 조회하면 404 응답을 반환한다")
    void findFailWhenMessageNotFound() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        given(messageService.find(messageId))
                .willThrow(new MessageNotFoundException(messageId));

        // when & then
        mockMvc.perform(
                        get("/messages/{id}", messageId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("메시지를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MessageNotFoundException"))
                .andExpect(jsonPath("$.details.messageId")
                        .value(messageId.toString()));

        then(messageService)
                .should()
                .find(messageId);
    }

    @Test
    @DisplayName("채널 ID로 메시지 목록을 조회한다")
    void findAllByChannelIdSuccess() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserDto author = new UserDto(
                userId,
                "user01",
                "user01@example.com",
                null,
                true
        );

        MessageDto message = new MessageDto(
                messageId,
                Instant.now(),
                Instant.now(),
                "조회된 메시지",
                channelId,
                author,
                List.of()
        );

        PageResponse<MessageDto> response = new PageResponse<>(
                List.of(message),
                0,
                1,
                1L
        );

        given(messageService.findAllByChannelId(channelId, 0))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/messages")
                                .param("channelId", channelId.toString())
                                .param("page", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id")
                        .value(messageId.toString()))
                .andExpect(jsonPath("$.content[0].content")
                        .value("조회된 메시지"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        then(messageService)
                .should()
                .findAllByChannelId(channelId, 0);
    }

    @Test
    @DisplayName("유효한 요청으로 메시지를 수정하면 변경된 정보를 반환한다")
    void updateSuccess() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserDto author = new UserDto(
                userId,
                "user01",
                "user01@example.com",
                null,
                true
        );

        MessageDto response = new MessageDto(
                messageId,
                Instant.now(),
                Instant.now(),
                "수정된 메시지",
                channelId,
                author,
                List.of()
        );

        given(messageService.update(
                eq(messageId),
                any(UpdateMessageRequest.class)
        )).willReturn(response);

        String requestJson = """
                {
                  "content": "수정된 메시지"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/messages/{id}", messageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("수정된 메시지"))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(userId.toString()))
                .andExpect(jsonPath("$.attachments").isArray());

        then(messageService)
                .should()
                .update(
                        eq(messageId),
                        any(UpdateMessageRequest.class)
                );
    }
}