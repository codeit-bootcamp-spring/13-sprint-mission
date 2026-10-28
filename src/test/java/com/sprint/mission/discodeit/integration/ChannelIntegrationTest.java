package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest // Spring 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("채널 API 통합 테스트")
@Transactional
public class ChannelIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ChannelService channelService;

  @Autowired
  UserService userService;

  @Nested
  @DisplayName("채널 생성 API 통합 테스트")
  class Create {

    @Test
    @DisplayName("공개 채널 정보를 정상적으로 등록할 수 있다")
    void 공개_채널_정상_등록() throws Exception {

      mvc.perform(post("/api/channels/public").contentType(APPLICATION_JSON)
              .content("""
                    {"name": "공개 채널", "description": "공개 채널 설명"}
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", notNullValue()))
          .andExpect(jsonPath("$.type", is(ChannelType.PUBLIC.name())))
          .andExpect(jsonPath("$.name", is("공개 채널")))
          .andExpect(jsonPath("$.description", is("공개 채널 설명")));
    }

    @Test
    @DisplayName("비공개 채널 정보를 정상적으로 등록할 수 있다")
    void 비공개_채널_정상_등록() throws Exception {

      UserCreateRequest userCreateRequest1 = new UserCreateRequest("첫번째 사용자",
          "firstUser@icloud.com", "Abcd1234!");
      UserCreateRequest userCreateRequest2 = new UserCreateRequest("두번째 사용자",
          "secondUser@icloud.com", "Efgh1234!");

      UserDto user1 = userService.create(userCreateRequest1, Optional.empty());
      UserDto user2 = userService.create(userCreateRequest2, Optional.empty());

      mvc.perform(post("/api/channels/private").contentType(APPLICATION_JSON)
              .content("""
                    {"participantIds": ["%S","%S"]}
                  """.formatted(user1.getId().toString(), user2.getId().toString())))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", notNullValue()))
          .andExpect(jsonPath("$.type", is(ChannelType.PRIVATE.name())))
          .andExpect(jsonPath("$.participants", hasSize(2)));
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
          .andExpect(jsonPath("$.code", is("PARAM_ERROR")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("채널 정보 수정 API 통합 테스트")
  class Update {

    @Test
    @DisplayName("공개 채널 정보를 수정할 수 있다")
    void 공개_채널_정상_수정() throws Exception {

      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);
      UUID channelId = channel.getId();

      mvc.perform(
              patch("/api/channels/{channelId}", channelId).contentType(APPLICATION_JSON)
                  .content(
                      """
                          {"newName": "공개 채널 수정", "newDescription": "공개 채널 설명 수정"}
                          """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(channelId.toString())))
          .andExpect(jsonPath("$.type", is(ChannelType.PUBLIC.name())))
          .andExpect(jsonPath("$.name", is("공개 채널 수정")))
          .andExpect(jsonPath("$.description", is("공개 채널 설명 수정")));
    }

    @Test
    @DisplayName("존재하지 않는 채널에 대해 수정을 시도하면 검증에 실패한다")
    void 공개_채널_수정() throws Exception {
      UUID nonChannelId = UUID.randomUUID();

      mvc.perform(patch("/api/channels/{channelId}", nonChannelId).contentType(APPLICATION_JSON)
              .content("""
                          {"newName": "공개 채널 수정", "newDescription": "공개 채널 설명 수정"
                          }
                  """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("CHANNEL_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("채널 정보 삭제 통합 API 테스트")
  class Delete {

    @Test
    @DisplayName("채널 정보를 삭제할 수 있다")
    void 채널_정상_삭제() throws Exception {
      PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 설명");
      ChannelDto channel = channelService.createPublicChannel(channelCreateRequest);
      UUID channelId = channel.getId();

      mvc.perform(
              delete("/api/channels/{channelId}", channelId))
          .andExpect(status().isNoContent());

      UserCreateRequest userCreateRequest = new UserCreateRequest("사용자",
          "user@icloud.com", "Abcd1234!");
      UserDto user = userService.create(userCreateRequest, Optional.empty());
      mvc.perform(get("/api/channels")
              .param("userId", user.getId().toString())
              .contentType(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[?(@.id == '" + channelId + "')]").doesNotExist());
    }

    @Test
    @DisplayName("존재하지 않은 채널 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_채널_삭제() throws Exception {
      UUID nonChannelId = UUID.randomUUID();

      mvc.perform(delete("/api/channels/{channelId}", nonChannelId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("CHANNEL_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("사용자가 참여 중인 전체 채널 목록 조회 API 통합 테스트")
  class FindAllByUserId {

    @Test
    @DisplayName("현재 사용자가 참여하고 있는 채널을 조회할 수 있다")
    void 참여_채널_목록_정상_조회() throws Exception {

      UserCreateRequest userCreateRequest1 = new UserCreateRequest("첫번째 사용자",
          "firstUser@icloud.com", "Abcd1234!");
      UserCreateRequest userCreateRequest2 = new UserCreateRequest("두번째 사용자",
          "secondUser@icloud.com", "Efgh1234!");

      UserDto user1 = userService.create(userCreateRequest1, Optional.empty());
      UserDto user2 = userService.create(userCreateRequest2, Optional.empty());
      UUID userId = user1.getId();

      PublicChannelCreateRequest publicChannelCreateRequest = new PublicChannelCreateRequest(
          "공개 채널",
          "공개 채널 설명");
      PrivateChannelCreateRequest privateChannelCreateRequest = new PrivateChannelCreateRequest(
          List.of(userId));
      channelService.createPublicChannel(publicChannelCreateRequest);
      channelService.createPrivateChannel(privateChannelCreateRequest);

      mvc.perform(get("/api/channels").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].type", is("PUBLIC")))
          .andExpect(jsonPath("$[0].name", is("공개 채널")))
          .andExpect(jsonPath("$[0].description", is("공개 채널 설명")))
          .andExpect(jsonPath("$[1].type", is("PRIVATE")))
          .andExpect(jsonPath("$[1].name").isEmpty())
          .andExpect(jsonPath("$[1].description").isEmpty())
          .andExpect(jsonPath("$[1].participants", hasSize(1)))
          .andExpect(jsonPath("$[1].participants[0].id", is(userId.toString())));
    }
  }
}
