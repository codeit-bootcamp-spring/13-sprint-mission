package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @Test
  @DisplayName("메시지를 생성하면 201 응답을 반환한다")
  void 메시지_생성_성공() throws Exception {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        """
            {
              "content": "테스트 메시지입니다.",
              "channelId": "%s",
              "authorId": "%s"
            }
            """.formatted(channelId, authorId)
            .getBytes(StandardCharsets.UTF_8)
    );

    MessageDto response = mock(MessageDto.class);

    given(
        messageService.create(
            any(MessageCreateRequest.class),
            isNull()
        )
    ).willReturn(response);

    // when & then
    mockMvc.perform(
            multipart("/api/messages")
                .file(requestPart)
        )
        .andExpect(status().isCreated())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            content().json(
                objectMapper.writeValueAsString(response)
            )
        );

    then(messageService).should()
        .create(
            any(MessageCreateRequest.class),
            isNull()
        );
  }

  @Test
  @DisplayName("존재하지 않는 메시지를 조회하면 404 응답을 반환한다")
  void 메시지_조회_실패_존재하지_않음() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    given(messageService.findById(messageId))
        .willThrow(new MessageNotFoundException(messageId));

    // when & then
    mockMvc.perform(
            get("/api/messages/{messageId}", messageId)
        )
        .andExpect(status().isNotFound())
        .andExpect(
            content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON
            )
        )
        .andExpect(
            jsonPath("$.code")
                .value("M001")
        )
        .andExpect(
            jsonPath("$.status")
                .value(404)
        )
        .andExpect(
            jsonPath("$.exceptionType")
                .value("MessageNotFoundException")
        );

    then(messageService).should()
        .findById(messageId);
  }
}