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

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
@DisplayName("사용자 API 통합 테스트")
@Transactional
public class UserIntegrationTest { // 사용자 관련 API 생성, 수정, 삭제, 목록 조회

  @Autowired
  MockMvc mvc;

  @Autowired
  UserService service;

  @Nested
  @DisplayName("사용자 생성 API 통합 테스트")
  class Create {

    @Test
    @DisplayName("프로필과 함께 사용자 정보를 정상적으로 생성할 수 있다")
    void 사용자_정상_생성() throws Exception {

      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "username":"사용자", "email":"user@icloud.com", "password":"Abcd1234!" }
              """.getBytes(StandardCharsets.UTF_8));
      MockMultipartFile profileRequest = new MockMultipartFile("profile", "profile.png",
          IMAGE_PNG_VALUE, "image".getBytes());

      mvc.perform(multipart("/api/users").file(userCreateRequest).file(profileRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", notNullValue()))
          .andExpect(jsonPath("$.username", is("사용자")))
          .andExpect(jsonPath("$.email", is("user@icloud.com")))
          .andExpect(jsonPath("$.profile.fileName", is("profile.png")))
          .andExpect(jsonPath("$.online", is(true)));
    }

    @Test
    @DisplayName("유효하지 않은 이메일 정보에 대해 등록을 요청하면 생성에 실패한다")
    void 유효하지_않은_이메일_등록() throws Exception {

      MockMultipartFile userCreateRequest = new MockMultipartFile("userCreateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "username":"사용자", "email":"invalid", "password":"Abcd1234!" }
              """.getBytes(StandardCharsets.UTF_8));

      mvc.perform(multipart("/api/users").file(userCreateRequest))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code", is("PARAM_ERROR")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("사용자 수정 API 통합 테스트")
  class Update {

    @Test
    @DisplayName("사용자는 프로필과 함께 정보에 대한 수정 요청을 보낼 수 있다")
    void 사용자_데이터_정상_수정() throws Exception {
      BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
          "profile.png", IMAGE_PNG_VALUE, "updated".getBytes());
      UserCreateRequest userCreateRequest = new UserCreateRequest("사용자",
          "user@icloud.com", "Abcd1234!");

      UserDto user = service.create(userCreateRequest, Optional.of(binaryContentCreateRequest));
      UUID userId = user.getId();

      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "newUsername":"수정된 사용자", "newEmail":"updatedUser@icloud.com", "newPassword":"Efgh1234!" }
              """.getBytes(StandardCharsets.UTF_8));
      MockMultipartFile profileRequest = new MockMultipartFile("profile", "updatedProfile.png",
          IMAGE_PNG_VALUE, "updatedImage".getBytes());

      mvc.perform(
              multipart("/api/users/{userId}", userId).file(userUpdateRequest)
                  .file(profileRequest)
                  .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                  }))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(userId.toString())))
          .andExpect(jsonPath("$.username", is("수정된 사용자")))
          .andExpect(jsonPath("$.email", is("updatedUser@icloud.com")))
          .andExpect(jsonPath("$.profile.fileName", is("updatedProfile.png")));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보로 수정을 요청하면 검증에 실패한다")
    void 유효하지_않은_사용자_수정() throws Exception {
      UUID nonUserId = UUID.randomUUID();
      MockMultipartFile userUpdateRequest = new MockMultipartFile("userUpdateRequest", "",
          APPLICATION_JSON_VALUE,
          """
              { "newUsername":"수정된 사용자", "newEmail":"updatedUser@icloud.com", "newPassword":"Efgh1234!" }
              """.getBytes(StandardCharsets.UTF_8));

      mvc.perform(multipart("/api/users/{userId}", nonUserId).file(userUpdateRequest)
              .with(request -> {
                request.setMethod("PATCH");
                return request;
              }))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("USER_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("사용자 삭제 API 통합 테스트")
  class Delete {

    @Test
    @DisplayName("사용자 정보를 삭제 요청을 할 수 있다")
    void 사용자_정보_정상_삭제() throws Exception {
      UserCreateRequest userCreateRequest = new UserCreateRequest("삭제할 사용자",
          "deletedUser@icloud.com", "Efgh1234!");

      UserDto user = service.create(userCreateRequest, Optional.empty());
      UUID userId = user.getId();

      mvc.perform(
              delete("/api/users/{userId}", userId))
          .andExpect(status().isNoContent());
      mvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[?(@.id == '" + userId + "')]").doesNotExist());

    }

    @Test
    @DisplayName("존재하지 않은 사용자 정보로 삭제를 시도하면 검증에 실패한다")
    void 존재하지_않는_사용자_삭제() throws Exception {
      UUID nonUserId = UUID.randomUUID();

      mvc.perform(delete("/api/users/{userId}", nonUserId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("USER_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }


  @Nested
  @DisplayName("사용자 전체 목록 조회 API 통합 테스트")
  class FindAll {

    @Test
    @DisplayName("전체 사용자 데이터를 조회할 수 있다")
    void 사용자_목록_정상_조회() throws Exception {

      BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
          "profile.png", IMAGE_PNG_VALUE, "profile".getBytes());

      UserCreateRequest userCreateRequest1 = new UserCreateRequest("첫번째 사용자",
          "firstUser@icloud.com", "Abcd1234!");
      UserCreateRequest userCreateRequest2 = new UserCreateRequest("두번째 사용자",
          "secondUser@icloud.com", "Efgh1234!");

      service.create(userCreateRequest1, Optional.of(binaryContentCreateRequest));
      service.create(userCreateRequest2, Optional.of(binaryContentCreateRequest));

      mvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].username", is("첫번째 사용자")))
          .andExpect(jsonPath("$[0].email", is("firstUser@icloud.com")))
          .andExpect(jsonPath("$[0].profile.fileName", is("profile.png")))
          .andExpect(jsonPath("$[0].online", is(true)))
          .andExpect(jsonPath("$[1].username", is("두번째 사용자")))
          .andExpect(jsonPath("$[1].email", is("secondUser@icloud.com")))
          .andExpect(jsonPath("$[1].profile.fileName", is("profile.png")))
          .andExpect(jsonPath("$[1].online", is(true)));
    }
  }


  @Nested
  @DisplayName("사용자의 온라인 상태 업데이트 API 통합 테스트")
  class updateOnlineStatus {

    @Test
    @DisplayName("사용자의 온라인 상태를 업데이트할 수 있다")
    void 사용자_온라인_상태_수정() throws Exception {

      UserCreateRequest userCreateRequest = new UserCreateRequest("사용자",
          "user@icloud.com", "Abcd1234!");

      UserDto user = service.create(userCreateRequest, Optional.empty());
      UUID userId = user.getId();

      Instant newLastActiveAt = Instant.parse("2023-01-01T00:00:00.00Z");

      mvc.perform(
              patch("/api/users/{userId}/userStatus", userId)
                  .contentType(APPLICATION_JSON)
                  .content("""
                      {"newLastActiveAt":"2023-01-01T00:00:00.00Z"}
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userId", is(userId.toString())))
          .andExpect(jsonPath("$.lastActiveAt", is(newLastActiveAt.toString())));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 온라인 상태는 업데이트할 수 없다")
    void 존재하지_않는_사용자_상태_수정() throws Exception {
      UUID nonUserId = UUID.randomUUID();

      mvc.perform(patch("/api/users/{userId}/userStatus", nonUserId).contentType(APPLICATION_JSON)
              .content("""
                  {"newLastActiveAt":"2023-01-01T00:00:00.00Z"}
                  """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code", is("USERSTATUS_NOT_FOUND")))
          .andExpect(jsonPath("$.details", notNullValue()));
    }
  }
}