package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.UpdateMessageCommand;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @Test
    @DisplayName("메시지 목록 조회에 성공하면 페이징 JSON을 반환한다")
    void getMessages_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        MessageDto messageDto = new MessageDto(
                messageId,
                createdAt,
                null,
                "안녕하세요",
                channelId,
                null,
                List.of()
        );

        PageResponse<MessageDto> response = PageResponse.of(
                List.of(messageDto),
                null,
                50,
                null,
                false
        );

        given(messageService.getMessages(
                eq(channelId),
                isNull(),
                any(Pageable.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/messages")
                                .param("channelId", channelId.toString())
                                .param("page", "0")
                                .param("size", "50")
                                .param("sort", "createdAt,desc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(messageId.toString())
                )
                .andExpect(
                        jsonPath("$.content[0].content")
                                .value("안녕하세요")
                )
                .andExpect(
                        jsonPath("$.content[0].channelId")
                                .value(channelId.toString())
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(50)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.hasNext")
                                .value(false)
                );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        then(messageService)
                .should()
                .getMessages(
                        eq(channelId),
                        isNull(),
                        pageableCaptor.capture()
                );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber())
                .isZero();

        assertThat(pageable.getPageSize())
                .isEqualTo(50);

        Sort.Order createdAtOrder =
                pageable.getSort().getOrderFor("createdAt");

        assertThat(createdAtOrder)
                .isNotNull();

        assertThat(createdAtOrder.getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("조회된 메시지가 없으면 빈 목록을 반환한다")
    void getMessages_empty() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();

        PageResponse<MessageDto> response = PageResponse.of(
                List.of(),
                null,
                50,
                null,
                false
        );

        given(messageService.getMessages(
                eq(channelId),
                isNull(),
                any(Pageable.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/messages")
                                .param("channelId", channelId.toString())
                                .param("page", "0")
                                .param("size", "50")
                                .param("sort", "createdAt,desc")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.content")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(50)
                )
                .andExpect(
                        jsonPath("$.hasNext")
                                .value(false)
                );

        then(messageService)
                .should()
                .getMessages(
                        eq(channelId),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("메시지 수정에 성공하면 200과 수정된 JSON을 반환한다")
    void update_success() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        Instant createdAt = Instant.now();
        Instant updatedAt = createdAt.plusSeconds(10);

        UpdateMessageRequest request =
                new UpdateMessageRequest(
                        "잘 부탁드립니다."
                );

        MessageDto response = new MessageDto(
                messageId,
                createdAt,
                updatedAt,
                "잘 부탁드립니다.",
                channelId,
                null,
                List.of()
        );

        given(messageService.update(
                eq(messageId),
                any(UpdateMessageCommand.class)
        )).willReturn(response);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(messageId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("잘 부탁드립니다.")
                )
                .andExpect(
                        jsonPath("$.channelId")
                                .value(channelId.toString())
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists()
                );

        ArgumentCaptor<UpdateMessageCommand> commandCaptor =
                ArgumentCaptor.forClass(UpdateMessageCommand.class);

        then(messageService)
                .should()
                .update(
                        eq(messageId),
                        commandCaptor.capture()
                );

        UpdateMessageCommand capturedCommand =
                commandCaptor.getValue();

        assertThat(capturedCommand.content())
                .isEqualTo("잘 부탁드립니다.");
    }

    @Test
    @DisplayName("메시지 내용이 공백이면 400을 반환한다")
    void update_fail_contentBlank() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        UpdateMessageRequest request =
                new UpdateMessageRequest(" ");

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
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
                        jsonPath("$.details.content")
                                .value("메시지 내용은 필수입니다.")
                );

        then(messageService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("메시지 내용이 null이면 400을 반환한다")
    void update_fail_contentNull() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        UpdateMessageRequest request =
                new UpdateMessageRequest(null);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
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
                        jsonPath("$.details.content")
                                .value("메시지 내용은 필수입니다.")
                );

        then(messageService)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 수정하면 404를 반환한다")
    void update_fail_messageNotFound() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        UpdateMessageRequest request =
                new UpdateMessageRequest(
                        "반갑습니다."
                );

        given(messageService.update(
                eq(messageId),
                any(UpdateMessageCommand.class)
        )).willThrow(
                new MessageNotFoundException(messageId)
        );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("MESSAGE_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.messageId")
                                .value(messageId.toString())
                );

        then(messageService)
                .should()
                .update(
                        eq(messageId),
                        any(UpdateMessageCommand.class)
                );
    }

    @Test
    @DisplayName("메시지 ID가 UUID 형식이 아니면 400을 반환한다")
    void update_fail_invalidMessageId() throws Exception {
        // given
        UpdateMessageRequest request =
                new UpdateMessageRequest(
                        "반갑습니다."
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                "invalid-message-id"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        then(messageService)
                .shouldHaveNoInteractions();
    }
}