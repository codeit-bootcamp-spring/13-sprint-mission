package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("MessageController 슬라이스 테스트")
class MessageControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  MessageService messageService;

  @Test
  @DisplayName("올바른 메시지 생성 요청이면 201과 메시지 정보를 반환한다")
  void createMessage() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    MessageCreateRequest request =
        new MessageCreateRequest("안녕하세요", authorId, channelId);
    MessageResponse response = new MessageResponse(
        messageId, null, null, "안녕하세요", channelId, null, List.of());
    MockMultipartFile requestPart = jsonPart("messageCreateRequest", request);

    given(messageService.createMessage(any(MessageCreateRequest.class), isNull()))
        .willReturn(response);

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(messageId.toString()))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()))
        .andExpect(jsonPath("$.content").value("안녕하세요"));
  }

  @Test
  @DisplayName("메시지 내용이 비어 있으면 400과 검증 오류를 반환한다")
  void rejectBlankContent() throws Exception {
    // given
    MessageCreateRequest invalidRequest =
        new MessageCreateRequest("", UUID.randomUUID(), UUID.randomUUID());
    MockMultipartFile requestPart =
        jsonPart("messageCreateRequest", invalidRequest);

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.details.fieldErrors").isArray());

    then(messageService).shouldHaveNoInteractions();
  }

  private MockMultipartFile jsonPart(String name, Object value) throws Exception {
    return new MockMultipartFile(
        name,
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(value));
  }
}
