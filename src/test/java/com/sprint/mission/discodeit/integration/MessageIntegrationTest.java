package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest // Spring 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("메시지 API 통합 테스트")
@Transactional
public class MessageIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  MessageService messageService;

  @Autowired
  ChannelService channelService;

  @Autowired
  UserService userService;

  @Nested
  @DisplayName("메시지 생성 통합 테스트 API")
  class Create {

    @Test
    @DisplayName("메시지 정보를 정상적으로 등록할 수 있다")
    void 메시지_정상_등록() throws Exception {
      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);

      UserCreateRequest userCreateRequest = new UserCreateRequest("작성자",
          "user@icloud.com", "Abcd1234!");
      UserDto user = userService.create(userCreateRequest, Optional.empty());

      MockMultipartFile messageCreateRequest = new MockMultipartFile("messageCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "content":"메시지 내용", "channelId": "%s", "authorId": "%s"}
              """.formatted(channel.getId(), user.getId()).getBytes(StandardCharsets.UTF_8));
      MockMultipartFile attachmentsRequest = new MockMultipartFile("attachments", "file.png",
          IMAGE_PNG_VALUE, "file".getBytes());

      mvc.perform(multipart("/api/messages").file(messageCreateRequest).file(attachmentsRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", notNullValue()))
          .andExpect(jsonPath("$.channelId", is(channel.getId().toString())))
          .andExpect(jsonPath("$.author.id", is(user.getId().toString())))
          .andExpect(jsonPath("$.author.username", is("작성자")))
          .andExpect(jsonPath("$.content", is("메시지 내용")))
          .andExpect(jsonPath("$.attachments", hasSize(1)))
          .andExpect(jsonPath("$.attachments[0].fileName", is("file.png")));
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
          .andExpect(jsonPath("$.code", is("PARAM_ERROR")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("메시지 정보 수정 통합 API")
  class Update {

    @Test
    @DisplayName("메시지 내용을 수정할 수 있다")
    void 메시지_내용_정상_수정() throws Exception {
      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);

      UserCreateRequest userCreateRequest = new UserCreateRequest("작성자",
          "author@icloud.com", "Abcd1234!");
      UserDto user = userService.create(userCreateRequest, Optional.empty());

      MessageCreateRequest messageCreateRequest = new MessageCreateRequest("메시지 내용",
          channel.getId(), user.getId());

      MessageDto message = messageService.create(messageCreateRequest, List.of());
      UUID messageId = message.getId();

      mvc.perform(
              patch("/api/messages/{messageId}", messageId).contentType(APPLICATION_JSON)
                  .content("""
                      {
                        "newContent": "수정한 메시지 내용"
                      }
                      """
                  ))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(messageId.toString())))
          .andExpect(jsonPath("$.content", is("수정한 메시지 내용")))
          .andExpect(jsonPath("$.updatedAt").exists())
          .andExpect(jsonPath("$.author.username", is("작성자")))
          .andExpect(jsonPath("$.author.email", is("author@icloud.com")))
          .andExpect(jsonPath("$.attachments").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 메시지로 수정을 시도하면 검증에 실패한다")
    void 존재하지_않는_메시지_내용_수정() throws Exception {
      UUID nonmessageId = UUID.randomUUID();

      mvc.perform(patch("/api/messages/{messageId}", nonmessageId).contentType(APPLICATION_JSON)
              .content(
                  """
                          {
                          "newContent": "메시지 내용"
                          }
                      """
              ))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("MESSAGE_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("메시지 삭제 통합 API")
  class Delete {

    @Test
    @DisplayName("메시지 정보를 삭제할 수 있다")
    void 메시지_정보_정상_삭제() throws Exception {
      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);

      UserCreateRequest userCreateRequest = new UserCreateRequest("작성자",
          "author@icloud.com", "Abcd1234!");
      UserDto user = userService.create(userCreateRequest, Optional.empty());

      MessageCreateRequest messageCreateRequest = new MessageCreateRequest("메시지 내용",
          channel.getId(), user.getId());

      MessageDto message = messageService.create(messageCreateRequest, List.of());
      UUID messageId = message.getId();

      mvc.perform(
              delete("/api/messages/{messageId}", messageId))
          .andExpect(status().isNoContent());
      mvc.perform(get("/api/messages").param("channelId", channel.getId().toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("존재하지 않은 메시지 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_메시지_삭제() throws Exception {
      UUID nonMessageId = UUID.randomUUID();

      mvc.perform(delete("/api/messages/{messageId}", nonMessageId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("MESSAGE_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }

  @Nested
  @DisplayName("전체 메시지 목록 조회 통합 API")
  class FindAll {

    @Test
    @DisplayName("전체 메시지 정보를 조회할 수 있다")
    void 메시지_목록_정상_조회() throws Exception {

      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);

      UserCreateRequest userCreateRequest = new UserCreateRequest("작성자",
          "author@icloud.com", "Abcd1234!");
      UserDto user = userService.create(userCreateRequest, Optional.empty());

      MessageCreateRequest messageCreateRequest1 = new MessageCreateRequest("첫번째 메시지 내용",
          channel.getId(), user.getId());
      MessageCreateRequest messageCreateRequest2 = new MessageCreateRequest("두번째 메시지 내용",
          channel.getId(), user.getId());

      messageService.create(messageCreateRequest1, List.of());
      messageService.create(messageCreateRequest2, List.of());

      mvc.perform(get("/api/messages")
              .param("channelId", channel.getId().toString())
              .contentType(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)))
          .andExpect(jsonPath("$.content[0].content", is("두번째 메시지 내용")))
          .andExpect(jsonPath("$.content[1].content", is("첫번째 메시지 내용")))
          .andExpect(jsonPath("$.number").exists())
          .andExpect(jsonPath("$.size").exists())
          .andExpect(jsonPath("$.hasNext").exists())
          .andExpect(jsonPath("$.totalElements").isEmpty());
    }
  }
}
