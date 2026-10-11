package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ErrorCodeStatusMapper.class)
@ActiveProfiles("test")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createMessage_유효한_요청과_첨부파일이면_201을_반환한다()
            throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        MessageCreateRequest request =
                new MessageCreateRequest(
                        channelId,
                        senderId,
                        "안녕하세요.",
                        List.of()
                );

        MessageResponse response = new MessageResponse(
                messageId,
                channelId,
                senderId,
                "안녕하세요.",
                List.of(),
                null,
                null,
                null
        );

        MockMultipartFile requestPart =
                new MockMultipartFile(
                        "messageCreateRequest",
                        "",
                        MediaType.APPLICATION_JSON_VALUE,
                        objectMapper.writeValueAsBytes(request)
                );

        byte[] attachmentBytes =
                "attachment-content".getBytes();

        MockMultipartFile attachmentPart =
                new MockMultipartFile(
                        "attachments",
                        "attachment.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        attachmentBytes
                );

        given(messageService.create(
                any(MessageCreateRequest.class),
                anyList()
        )).willReturn(response);

        mockMvc.perform(
                        multipart("/api/messages")
                                .file(requestPart)
                                .file(attachmentPart)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(messageId.toString())
                )
                .andExpect(
                        jsonPath("$.channelId")
                                .value(channelId.toString())
                )
                .andExpect(
                        jsonPath("$.senderId")
                                .value(senderId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("안녕하세요.")
                )
                .andExpect(
                        jsonPath("$.attachments").isArray()
                );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BinaryContentRequest>>
                attachmentCaptor =
                ArgumentCaptor.forClass(List.class);

        then(messageService).should().create(
                any(MessageCreateRequest.class),
                attachmentCaptor.capture()
        );

        List<BinaryContentRequest> capturedAttachments =
                attachmentCaptor.getValue();

        assertThat(capturedAttachments).hasSize(1);

        BinaryContentRequest capturedAttachment =
                capturedAttachments.get(0);

        assertThat(capturedAttachment.fileName())
                .isEqualTo("attachment.txt");
        assertThat(capturedAttachment.size())
                .isEqualTo((long) attachmentBytes.length);
        assertThat(capturedAttachment.contentType())
                .isEqualTo(MediaType.TEXT_PLAIN_VALUE);
        assertThat(capturedAttachment.bytes())
                .isEqualTo(attachmentBytes);
    }

    @Test
    void updateMessage_내용이_비어있으면_400과_검증_오류_JSON을_반환한다()
            throws Exception {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request =
                new MessageUpdateRequest("");

        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsBytes(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_FAILED")
                )
                .andExpect(
                        jsonPath(
                                "$.details.errors[0].field"
                        ).value("content")
                );

        then(messageService).shouldHaveNoInteractions();
    }
}