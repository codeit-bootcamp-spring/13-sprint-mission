package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@DisplayName("ChannelController 슬라이스 테스트")
public class ChannelControllerTest {

  @Autowired
  MockMvc mvc; // 진짜 서버를 띄우지 않고 HTTP 요청과 응답을 가짜로 시뮬레이션 해 준다

  @MockitoBean // 의존 관계가 있는 객체들은 모두 가짜로 채운다
  ChannelService service;

  @Nested
  @DisplayName("POST (생성) - Public 채널 생성")
  class Create {

    @Test
    @DisplayName("공개 채널 정보를 정상적으로 등록할 수 있다")
    void 공개_채널_정상_등록() throws Exception {
      UUID channelId = UUID.randomUUID();
      Instant lastMessageAt = Instant.parse("2023-01-01T00:00:00.00Z");

      ChannelDto channel = ChannelDto.builder()
          .id(channelId)
          .type(ChannelType.PUBLIC)
          .name("공개 채널")
          .description("공개 채널 설명")
          .lastMessageAt(lastMessageAt)
          .participants(List.of())
          .build();
      given(service.createPublicChannel(any(PublicChannelCreateRequest.class))).willReturn(channel);

      mvc.perform(post("/api/channels/public").contentType(APPLICATION_JSON)
              .content("""
                    {"name": "공개 채널", "description": "공개 채널 설명"}
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
          .andExpect(jsonPath("$.name").value("공개 채널"))
          .andExpect(jsonPath("$.description").value("공개 채널 설명"))
          .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()))
          .andExpect(jsonPath("$.participants").isEmpty());
      verify(service).createPublicChannel(any(PublicChannelCreateRequest.class));
    }

    @Test
    @DisplayName("비공개 채널 정보를 정상적으로 등록할 수 있다")
    void 비공개_채널_정상_등록() throws Exception {
      UUID channelId = UUID.randomUUID();
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      UserDto user1 = UserDto.builder()
          .id(userId1)
          .username("첫번째 사용자")
          .email("firstUser@icloud.com")
          .profile(null)
          .online(false)
          .build();
      UserDto user2 = UserDto.builder()
          .id(userId2)
          .username("두번째 사용자")
          .email("secondUser@icloud.com")
          .profile(null)
          .online(false)
          .build();
      Instant lastMessageAt = Instant.parse("2023-01-01T00:00:00.00Z");

      ChannelDto channel = ChannelDto.builder()
          .id(channelId)
          .type(ChannelType.PRIVATE)
          .name(null)
          .description(null)
          .lastMessageAt(lastMessageAt)
          .participants(List.of(user1, user2))
          .build();
      given(service.createPrivateChannel(any(PrivateChannelCreateRequest.class))).willReturn(
          channel);

      mvc.perform(post("/api/channels/private").contentType(APPLICATION_JSON)
              .content("""
                    {"participantIds": ["%S","%S"]}
                  """.formatted(userId1.toString(), userId2.toString())))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.participants.length()").value(2))
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.type").value(ChannelType.PRIVATE.name()))
          .andExpect(jsonPath("$.name").isEmpty())
          .andExpect(jsonPath("$.description").isEmpty())
          .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()))
          .andExpect(jsonPath("$.participants").isArray());
      verify(service).createPrivateChannel(any(PrivateChannelCreateRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 채널 정보에 대해 등록을 시도하면 검증에 실패한다")
    void 유효하지_않은_채널_등록() throws Exception {

      String channelName = "공개 채널".repeat(30);

      mvc.perform(post("/api/channels/public").contentType(APPLICATION_JSON)
              .content("""
                    {"name": "%s", "description": "공개 채널 설명"}
                  """.formatted(channelName)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PARAM_ERROR"))
          .andExpect(jsonPath("$.details").exists());
      verify(service, never()).createPublicChannel(
          any(PublicChannelCreateRequest.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다

    }
  }


  @Nested
  @DisplayName("UPDATE (수정) - 채널 정보 수정")
  class Update {

    @Test
    @DisplayName("공개 채널 정보를 수정할 수 있다")
    void 공개_채널_정상_수정() throws Exception {
      UUID channelId = UUID.randomUUID();
      Instant lastMessageAt = Instant.parse("2023-01-01T00:00:00.00Z");

      ChannelDto channel = ChannelDto.builder()
          .id(channelId)
          .type(ChannelType.PUBLIC)
          .name("공개 채널 수정")
          .description("공개 채널 설명 수정")
          .lastMessageAt(lastMessageAt)
          .participants(List.of())
          .build();
      given(
          service.update(eq(channelId), any(PublicChannelUpdateRequest.class))).willReturn(
          channel);

      mvc.perform(
              patch("/api/channels/{channelId}", channelId).contentType(APPLICATION_JSON)
                  .content(
                      """
                          {"newName": "공개 채널 수정", "newDescription": "공개 채널 설명 수정"}
                          """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
          .andExpect(jsonPath("$.name").value("공개 채널 수정"))
          .andExpect(jsonPath("$.description").value("공개 채널 설명 수정"))
          .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()))
          .andExpect(jsonPath("$.participants").isEmpty());
      verify(service).update(eq(channelId), any(PublicChannelUpdateRequest.class));
    }

    @Test
    @DisplayName("비공개 채널에 대해 수정을 시도하면 검증에 실패한다")
    void 비공개_채널_수정() throws Exception {
      UUID channelId = UUID.randomUUID();

      given(service.update(eq(channelId), any(PublicChannelUpdateRequest.class)))
          .willThrow(new PrivateChannelUpdateException(channelId));

      mvc.perform(patch("/api/channels/{channelId}", channelId).contentType(APPLICATION_JSON)
              .content("""
                          {"newName": "비공개 채널 수정", "newDescription": "비공개 채널 설명 수정"
                          }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PRIVATE_CHANNEL_UPDATE"))
          .andExpect(jsonPath("$.details").exists());
      verify(service).update(eq(channelId),
          any(PublicChannelUpdateRequest.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }


  @Nested
  @DisplayName("DELETE (삭제) - 채널 정보 삭제")
  class Delete {

    @Test
    @DisplayName("채널 정보를 삭제할 수 있다")
    void 채널_정상_삭제() throws Exception {
      UUID channelId = UUID.randomUUID();

      mvc.perform(
              delete("/api/channels/{channelId}", channelId))
          .andExpect(status().isNoContent());
      verify(service).delete(eq(channelId));
    }

    @Test
    @DisplayName("존재하지 않은 채널 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_채널_삭제() throws Exception {
      UUID channelId = UUID.randomUUID();

      willThrow(new ChannelNotFoundException(channelId))
          .given(service).delete(channelId);

      mvc.perform(delete("/api/channels/{channelId}", channelId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
          .andExpect(jsonPath("$.details").exists());
      verify(service).delete(channelId); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }


  @Nested
  @DisplayName("FindAllByUserId (조회) - 사용자가 참여 중인 전체 채널 목록 조회")
  class FindAllByUserId {

    @Test
    @DisplayName("현재 사용자가 참여하고 있는 채널을 조회할 수 있다")
    void 참여_채널_목록_정상_조회() throws Exception {
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();
      UUID channelId1 = UUID.randomUUID();
      UUID channelId2 = UUID.randomUUID();
      Instant lastMessageAt = Instant.parse("2023-01-01T00:00:00.00Z");

      UserDto user1 = UserDto.builder()
          .id(userId1)
          .username("첫번째 사용자")
          .email("firstUser@icloud.com")
          .profile(null)
          .online(false)
          .build();
      UserDto user2 = UserDto.builder()
          .id(userId2)
          .username("두번째 사용자")
          .email("secondUser@icloud.com")
          .profile(null)
          .online(false)
          .build();

      List<ChannelDto> channels = List.of(
          ChannelDto.builder()
              .id(channelId1)
              .type(ChannelType.PRIVATE)
              .name(null)
              .description(null)
              .lastMessageAt(lastMessageAt)
              .participants(List.of(user1, user2))
              .build(),
          ChannelDto.builder()
              .id(channelId2)
              .type(ChannelType.PUBLIC)
              .name("공개 채널")
              .description("공개 채널 설명")
              .lastMessageAt(lastMessageAt)
              .participants(List.of())
              .build()
      );

      given(service.findAllByUserId(userId1)).willReturn(channels);

      mvc.perform(get("/api/channels").param("userId", userId1.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(channelId1.toString()))
          .andExpect(jsonPath("$[0].type").value("PRIVATE"))
          .andExpect(jsonPath("$[0].name").isEmpty())
          .andExpect(jsonPath("$[0].description").isEmpty())
          .andExpect(jsonPath("$[0].lastMessageAt").value(lastMessageAt.toString()))
          .andExpect(jsonPath("$[0].participants[0].id").value(userId1.toString()))
          .andExpect(jsonPath("$[0].participants[1].id").value(userId2.toString()))
          .andExpect(jsonPath("$[1].id").value(channelId2.toString()))
          .andExpect(jsonPath("$[1].type").value("PUBLIC"))
          .andExpect(jsonPath("$[1].name").value("공개 채널"))
          .andExpect(jsonPath("$[1].description").value("공개 채널 설명"))
          .andExpect(jsonPath("$[1].lastMessageAt").value(lastMessageAt.toString()))
          .andExpect(jsonPath("$[1].participants").isEmpty());
      verify(service).findAllByUserId(userId1);
    }
  }
}
