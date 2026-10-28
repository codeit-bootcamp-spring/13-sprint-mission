package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@DisplayName("MessageController 슬라이스 테스트")
public class MessageControllerTest {

  @Autowired
  MockMvc mvc; // 진짜 서버를 띄우지 않고 HTTP 요청과 응답을 가짜로 시뮬레이션 해 준다

  @MockitoBean // 의존 관계가 있는 객체들은 모두 가짜로 채운다
  MessageService service;

  @Nested
  @DisplayName("POST (생성) - 메시지 생성")
  class Create {

    @Test
    @DisplayName("메시지 정보를 정상적으로 등록할 수 있다")
    void 메시지_정상_등록() throws Exception {
      UUID messageId = UUID.randomUUID();
      UUID channelId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID binaryContentId = UUID.randomUUID();
      Instant createdAt = Instant.now();

      UserDto author = UserDto.builder()
          .id(userId)
          .username("작성자")
          .email("author@icloud.com")
          .profile(null)
          .online(false)
          .build();
      BinaryContentDto attachment = BinaryContentDto.builder()
          .id(binaryContentId)
          .fileName("file.png")
          .contentType(MediaType.IMAGE_PNG_VALUE)
          .size(12L)
          .build();
      MessageDto message = MessageDto.builder()
          .id(messageId)
          .createdAt(createdAt)
          .updatedAt(createdAt)
          .channelId(channelId)
          .author(author)
          .content("메시지 내용")
          .attachments(List.of(attachment))
          .build();

      given(service.create(any(MessageCreateRequest.class), any(List.class))).willReturn(message);

      MockMultipartFile messageCreateRequest = new MockMultipartFile("messageCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "content":"메시지 내용", "channelId": "%s", "authorId": "%s"}
              """.formatted(channelId, userId).getBytes(StandardCharsets.UTF_8));
      MockMultipartFile attachmentsRequest = new MockMultipartFile("attachments", "file.png",
          IMAGE_PNG_VALUE, "file".getBytes());

      mvc.perform(multipart("/api/messages").file(messageCreateRequest).file(attachmentsRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(messageId.toString()))
          .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
          .andExpect(jsonPath("$.updatedAt").value(createdAt.toString()))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.author.id").value(userId.toString()))
          .andExpect(jsonPath("$.author.username").value("작성자"))
          .andExpect(jsonPath("$.content").value("메시지 내용"))
          .andExpect(jsonPath("$.attachments[0].id").value(binaryContentId.toString()))
          .andExpect(jsonPath("$.attachments[0].fileName").value("file.png"))
          .andExpect(jsonPath("$.attachments.length()").value(1));
    }

    @Test
    @DisplayName("유효하지 않은 메시지 정보에 대해 등록을 시도하면 검증에 실패한다")
    void 유효하지_않은_메시지_등록() throws Exception {

      String content = "메시지 내용".repeat(1000);

      MockMultipartFile messageCreateRequest = new MockMultipartFile("messageCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "content": "%s", "channelId": "%s", "authorId": "%s"}
              """.formatted(content, UUID.randomUUID(), UUID.randomUUID())
              .getBytes(StandardCharsets.UTF_8));

      mvc.perform(
              multipart("/api/messages").file(messageCreateRequest))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PARAM_ERROR"))
          .andExpect(jsonPath("$.details").exists());
      verify(service, never()).create(any(MessageCreateRequest.class),
          any(List.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }


  @Nested
  @DisplayName("UPDATE (수정) - 메시지 정보 수정")
  class Update {

    @Test
    @DisplayName("메시지 내용을 수정할 수 있다")
    void 메시지_내용_정상_수정() throws Exception {
      UUID messageId = UUID.randomUUID();
      Instant createdAt = Instant.now();
      UserDto author = UserDto.builder()
          .id(UUID.randomUUID())
          .username("작성자")
          .email("author@icloud.com")
          .profile(null)
          .online(false)
          .build();
      MessageDto message = MessageDto.builder()
          .id(messageId)
          .createdAt(createdAt)
          .updatedAt(createdAt)
          .channelId(UUID.randomUUID())
          .author(author)
          .content("수정한 메시지 내용")
          .attachments(List.of())
          .build();

      given(
          service.update(eq(messageId), any(MessageUpdateRequest.class))).willReturn(
          message);

      mvc.perform(
              patch("/api/messages/{messageId}", messageId).contentType(APPLICATION_JSON)
                  .content("""
                      {
                        "newContent": "수정한 메시지 내용"
                      }
                      """
                  ))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(messageId.toString()))
          .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
          .andExpect(jsonPath("$.updatedAt").value(createdAt.toString()))
          .andExpect(jsonPath("$.content").value("수정한 메시지 내용"))
          .andExpect(jsonPath("$.author.username").value("작성자"))
          .andExpect(jsonPath("$.author.email").value("author@icloud.com"))
          .andExpect(jsonPath("$.attachments").isEmpty());
      verify(service).update(eq(messageId), any(MessageUpdateRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 메시지 내용으로 수정을 시도하면 검증에 실패한다")
    void 유효하지_않은_메시지_내용_수정() throws Exception {
      UUID messageId = UUID.randomUUID();
      String newContent = "수정한 메시지 내용".repeat(1000);

      mvc.perform(patch("/api/messages/{messageId}", messageId).contentType(APPLICATION_JSON)
              .content(
                  """
                      {
                      "newContent": "%s"
                      }
                      """.formatted(newContent)
              ))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PARAM_ERROR"))
          .andExpect(jsonPath("$.details").exists());
      verify(service, never()).update(eq(messageId),
          any(MessageUpdateRequest.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }

  @Nested
  @DisplayName("DELETE (삭제) - 메시지 삭제")
  class Delete {

    @Test
    @DisplayName("메시지 정보를 삭제할 수 있다")
    void 메시지_정보_정상_삭제() throws Exception {
      UUID messageId = UUID.randomUUID();

      mvc.perform(
              delete("/api/messages/{messageId}", messageId))
          .andExpect(status().isNoContent());
      verify(service).delete(messageId);
    }

    @Test
    @DisplayName("존재하지 않은 메시지 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_메시지_삭제() throws Exception {
      UUID messageId = UUID.randomUUID();

      willThrow(new MessageNotFoundException(messageId))
          .given(service).delete(messageId);

      mvc.perform(delete("/api/messages/{messageId}", messageId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"))
          .andExpect(jsonPath("$.details").exists());
      verify(service).delete(messageId);
    }
  }


  @Nested
  @DisplayName("FindAll (조회) - 전체 메시지 목록 조회")
  class FindAll {

    @Test
    @DisplayName("전체 메시지 정보를 조회할 수 있다")
    void 메시지_목록_정상_조회() throws Exception {
      UUID messageId1 = UUID.randomUUID();
      UUID messageId2 = UUID.randomUUID();
      UUID channelId = UUID.randomUUID();
      Instant createdAt = Instant.parse("2023-01-01T00:00:00.00Z");

      UserDto author = UserDto.builder()
          .id(UUID.randomUUID())
          .username("작성자")
          .email("author@icloud.com")
          .profile(null)
          .online(false)
          .build();

      List<MessageDto> messages = List.of(
          MessageDto.builder()
              .id(messageId1)
              .createdAt(createdAt.minusSeconds(10))
              .updatedAt(createdAt.minusSeconds(10))
              .channelId(channelId)
              .author(author)
              .content("첫번째 메시지 내용")
              .attachments(List.of())
              .build(),
          MessageDto.builder()
              .id(messageId2)
              .createdAt(createdAt.minusSeconds(20))
              .updatedAt(createdAt.minusSeconds(20))
              .channelId(channelId)
              .author(author)
              .content("두번째 메시지 내용")
              .attachments(List.of())
              .build()
      );

      Pageable pageable = PageRequest.of(0, 50, Sort.by(Direction.DESC, "createdAt"));
      PageResponse<MessageDto> pageResponse = new PageResponse<>(
          messages,
          pageable.getPageNumber(),
          pageable.getPageSize(),
          false,
          (long) messages.size()
      );

      given(service.findAllByChannelId(eq(channelId), any(Pageable.class))).willReturn(
          pageResponse);

      mvc.perform(get("/api/messages")
              .param("channelId", channelId.toString())
              .contentType(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content.length()").value(2))
          .andExpect(jsonPath("$.content[0].content").value("첫번째 메시지 내용"))
          .andExpect(jsonPath("$.content[1].content").value("두번째 메시지 내용"))
          .andExpect(jsonPath("$.number").exists())
          .andExpect(jsonPath("$.size").value(50))
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.totalElements").value(2));
      verify(service).findAllByChannelId(eq(channelId), any(Pageable.class));
    }
  }
}
