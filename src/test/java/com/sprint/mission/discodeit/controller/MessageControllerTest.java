package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @DisplayName("메시지 생성 성공 - 201")
    void createMessage_성공() throws Exception {
        MessageDto response = new MessageDto(
                UUID.randomUUID(), null, null, "안녕", UUID.randomUUID(), null, null);
        given(messageService.create(any(), any())).willReturn(response);

        MessageCreateRequest request =
                new MessageCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "안녕");
        MockMultipartFile jsonPart = new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/messages")
                        .file(jsonPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("안녕"));
    }

    @Test
    @DisplayName("존재하지 않는 메시지 삭제 - 404")
    void deleteMessage_실패() throws Exception {
        UUID messageId = UUID.randomUUID();
        willThrow(MessageNotFoundException.withId(messageId))
                .given(messageService).delete(messageId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }
}