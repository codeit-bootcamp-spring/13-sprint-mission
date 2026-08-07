package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@DisplayName("UserController 슬라이스 테스트")
public class UserControllerTest {

  @Autowired
  MockMvc mvc; // 진짜 서버를 띄우지 않고 HTTP 요청과 응답을 가짜로 시뮬레이션 해 준다

  @MockitoBean // 의존 관계가 있는 객체들은 모두 가짜로 채운다
  UserService service;

  @MockitoBean
  UserStatusService statusService;

  @Nested
  @DisplayName("POST (생성) - 사용자 프로필 이미지 선택적 등록")
  class Create {

    @Test
    @DisplayName("사용자는 프로필과 함께 정보를 등록할 수 있다")
    void 사용자_정상_등록() throws Exception {
      UUID userId = UUID.randomUUID();
      BinaryContentDto profile = BinaryContentDto.builder()
          .id(UUID.randomUUID())
          .fileName("profile.png")
          .contentType(IMAGE_PNG_VALUE)
          .size(14L)
          .build();

      UserDto user = UserDto.builder()
          .id(userId)
          .username("사용자")
          .email("user@icloud.com")
          .profile(profile)
          .online(false)
          .build();
      given(service.create(any(UserCreateRequest.class), any(Optional.class))).willReturn(user);

      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "username":"사용자", "email":"user@icloud.com", "password":"Abcd1234!" }
              """.getBytes(StandardCharsets.UTF_8));
      MockMultipartFile profileRequest = new MockMultipartFile("profile", "profile.png",
          IMAGE_PNG_VALUE, "image".getBytes());

      mvc.perform(multipart("/api/users").file(userCreateRequest).file(profileRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value("사용자"))
          .andExpect(jsonPath("$.email").value("user@icloud.com"))
          .andExpect(jsonPath("$.profile.fileName").value("profile.png"))
          .andExpect(jsonPath("$.online").value(false));
    }


    @Test
    @DisplayName("유효하지 않은 사용자 정보에 대해 등록을 시도하면 검증에 실패한다")
    void 유효하지_않은_이메일_등록() throws Exception {

      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "username":"사용자", "email":"invalid", "password":"Abcd1234!" }
              """.getBytes(StandardCharsets.UTF_8));

      mvc.perform(multipart("/api/users").file(userCreateRequest))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PARAM_ERROR"))
          .andExpect(jsonPath("$.details").exists());
      verify(service, never()).create(any(UserCreateRequest.class),
          any(Optional.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다

    }
  }


  @Nested
  @DisplayName("UPDATE (수정) - 사용자 데이터, 프로필 수정")
  class Update {

    @Test
    @DisplayName("사용자는 프로필과 함께 데이터를 수정할 수 있다")
    void 사용자_데이터_정상_수정() throws Exception {
      UUID userId = UUID.randomUUID();
      BinaryContentDto profile = BinaryContentDto.builder()
          .id(UUID.randomUUID())
          .fileName("updatedProfile.png")
          .contentType(IMAGE_PNG_VALUE)
          .size(14L)
          .build();

      UserDto user = UserDto.builder()
          .id(userId)
          .username("수정된 사용자")
          .email("updatedUser@icloud.com")
          .profile(profile)
          .online(false)
          .build();
      given(
          service.update(eq(userId), any(UserUpdateRequest.class), any(Optional.class))).willReturn(
          user);

      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "newUsername":"수정된 사용자", "newEmail":"updatedUser@icloud.com", "newPassword":"Efgh1234!" }
              """.getBytes(StandardCharsets.UTF_8));
      MockMultipartFile profileRequest = new MockMultipartFile("profile", "updatedProfile.png",
          IMAGE_PNG_VALUE, "updatedImage".getBytes());

      mvc.perform(
              multipart("/api/users/{userId}", userId).file(userUpdateRequest).file(profileRequest)
                  .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                  }))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value("수정된 사용자"))
          .andExpect(jsonPath("$.email").value("updatedUser@icloud.com"))
          .andExpect(jsonPath("$.profile.fileName").value("updatedProfile.png"))
          .andExpect(jsonPath("$.online").value(false));
    }

    @Test
    @DisplayName("유효하지 않은 사용자 정보로 수정을 시도하면 검증에 실패한다")
    void 유효하지_않은_사용자_이름_수정() throws Exception {
      UUID userId = UUID.randomUUID();
      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "newUsername":".", "newEmail":"user@icloud.com", "newPassword":"Abcd1234!" }
              """.getBytes(StandardCharsets.UTF_8));

      mvc.perform(multipart("/api/users/{userId}", userId).file(userUpdateRequest)
              .with(request -> {
                request.setMethod("PATCH");
                return request;
              }))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("PARAM_ERROR"))
          .andExpect(jsonPath("$.details").exists());
      verify(service, never()).update(eq(userId), any(UserUpdateRequest.class),
          any(Optional.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }


  @Nested
  @DisplayName("DELETE (삭제) - 사용자 정보, 프로필 삭제")
  class Delete {

    @Test
    @DisplayName("사용자 정보를 삭제할 수 있다")
    void 사용자_정보_정상_삭제() throws Exception {
      UUID userId = UUID.randomUUID();

      mvc.perform(
              delete("/api/users/{userId}", userId))
          .andExpect(status().isNoContent());
      verify(service).delete(eq(userId));
    }

    @Test
    @DisplayName("존재하지 않은 사용자 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_사용자_삭제() throws Exception {
      UUID userId = UUID.randomUUID();

      willThrow(new UserNotFoundException(userId))
          .given(service).delete(userId);

      mvc.perform(delete("/api/users/{userId}", userId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
          .andExpect(jsonPath("$.details").exists());
      verify(service).delete(userId); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }


  @Nested
  @DisplayName("FindAll (조회) - 전체 사용자 목록 조회")
  class FindAll {

    @Test
    @DisplayName("전체 사용자 데이터를 조회할 수 있다")
    void 사용자_목록_정상_조회() throws Exception {
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();

      BinaryContentDto profile = BinaryContentDto.builder()
          .id(UUID.randomUUID())
          .fileName("profile.png")
          .contentType(IMAGE_PNG_VALUE)
          .size(14L)
          .build();

      UserDto user1 = UserDto.builder()
          .id(userId1)
          .username("첫번째 사용자")
          .email("firstUser@icloud.com")
          .profile(profile)
          .online(false)
          .build();
      UserDto user2 = UserDto.builder()
          .id(userId2)
          .username("두번째 사용자")
          .email("secondUser@icloud.com")
          .profile(profile)
          .online(false)
          .build();

      List<UserDto> users = List.of(user1, user2);
      given(service.findAll()).willReturn(users);

      mvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(userId1.toString()))
          .andExpect(jsonPath("$[0].username").value("첫번째 사용자"))
          .andExpect(jsonPath("$[0].email").value("firstUser@icloud.com"))
          .andExpect(jsonPath("$[0].profile.fileName").value("profile.png"))
          .andExpect(jsonPath("$[0].online").value(false))
          .andExpect(jsonPath("$[1].id").value(userId2.toString()))
          .andExpect(jsonPath("$[1].username").value("두번째 사용자"))
          .andExpect(jsonPath("$[1].email").value("secondUser@icloud.com"))
          .andExpect(jsonPath("$[1].profile.fileName").value("profile.png"))
          .andExpect(jsonPath("$[1].online").value(false));
      verify(service).findAll();
    }
  }


  @Nested
  @DisplayName("UpdateOnlineStatus (수정) - 사용자의 온라인 상태 업데이트")
  class updateOnlineStatus {

    @Test
    @DisplayName("사용자의 온라인 상태를 업데이트할 수 있다")
    void 사용자_온라인_상태_수정() throws Exception {
      UUID userId = UUID.randomUUID();
      UUID userStatusId = UUID.randomUUID();
      Instant lastActiveAt = Instant.parse("2023-01-01T00:00:00.00Z");

      UserStatusDto userStatus = UserStatusDto.builder()
          .id(userStatusId)
          .userId(userId)
          .lastActiveAt(lastActiveAt)
          .build();
      given(
          statusService.updateByUserId(eq(userId), any(UserStatusUpdateRequest.class))).willReturn(
          userStatus);

      mvc.perform(
              patch("/api/users/{userId}/userStatus", userId)
                  .contentType(APPLICATION_JSON)
                  .content("""
                      {"newLastActiveAt":"2023-01-01T00:00:00.00Z"}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userStatusId.toString()))
          .andExpect(jsonPath("$.userId").value(userId.toString()))
          .andExpect(jsonPath("$.lastActiveAt").value(lastActiveAt.toString()));
      verify(statusService).updateByUserId(eq(userId), any(UserStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 온라인 상태는 업데이트할 수 없다")
    void 존재하지_않는_사용자_상태_수정() throws Exception {
      UUID userId = UUID.randomUUID();

      given(statusService.updateByUserId(eq(userId), any(UserStatusUpdateRequest.class)))
          .willThrow(new UserNotFoundException(userId));

      mvc.perform(patch("/api/users/{userId}/userStatus", userId).contentType(APPLICATION_JSON)
              .content("""
                  {"newLastActiveAt":"2023-01-01T00:00:00.00Z"}
                  """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
          .andExpect(jsonPath("$.details").exists());
      verify(statusService).updateByUserId(eq(userId),
          any(UserStatusUpdateRequest.class)); // 검증에서 막히기 때문에 서비스까지 갈 수 없다
    }
  }
}
