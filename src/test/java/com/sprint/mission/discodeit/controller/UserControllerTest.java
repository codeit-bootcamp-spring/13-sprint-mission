package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.mapper.UserMultipartMapper;
import com.sprint.mission.discodeit.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Controller 테스트에서는 실제 서비스 로직을 실행하지 않는다.
     * UserService를 Mock Bean으로 등록해 Controller 로직만 검증한다.
     */
    @MockitoBean
    private UserService userService;

    /*
     * UserController 생성자에 필요한 의존성이다.
     * JSON API만 테스트하더라도 Controller Bean 생성을 위해 필요하다.
     */
    @MockitoBean
    private UserMultipartMapper userMultipartMapper;

    /*
     * 메인 애플리케이션의 @EnableJpaAuditing 때문에
     * @WebMvcTest 실행 시 JPA 메타모델을 생성하려는 문제를 방지한다.
     *
     * @WebMvcTest에서는 Entity와 Repository를 로딩하지 않으므로
     * 실제 JpaMetamodelMappingContext 대신 Mock Bean을 등록한다.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("사용자 생성")
    class Create {

        @Test
        @DisplayName("유효한 JSON 요청이면 사용자를 생성하고 201 Created를 반환한다")
        void success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            UserCreateRequest request = new UserCreateRequest(
                    "tester",
                    "tester@test.com",
                    "password",
                    null
            );

            UserResponse response = new UserResponse(
                    userId,
                    createdAt,
                    null,
                    "tester",
                    "tester@test.com",
                    null,
                    false
            );

            given(userService.create(any(UserCreateRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            "Location",
                            "/api/users/" + userId
                    ))
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(userId.toString()))
                    .andExpect(jsonPath("$.createdAt")
                            .value(createdAt.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("tester"))
                    .andExpect(jsonPath("$.email")
                            .value("tester@test.com"))
                    .andExpect(jsonPath("$.online")
                            .value(false));

            then(userService)
                    .should()
                    .create(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("사용자 생성 요청값이 유효하지 않으면 400 Bad Request를 반환한다")
        void invalidRequest() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "",
                    "invalid-email",
                    "",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(userService)
                    .should(never())
                    .create(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("username이 2자 미만이면 400 Bad Request를 반환한다")
        void usernameTooShort() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "a",
                    "tester@test.com",
                    "password",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(userService)
                    .should(never())
                    .create(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("비밀번호가 4자 미만이면 400 Bad Request를 반환한다")
        void passwordTooShort() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "tester",
                    "tester@test.com",
                    "123",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(userService)
                    .should(never())
                    .create(any(UserCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("사용자 단건 조회")
    class Read {

        @Test
        @DisplayName("존재하는 사용자를 조회하면 200 OK와 사용자 정보를 반환한다")
        void success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID profileId = UUID.randomUUID();

            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            Instant updatedAt =
                    Instant.parse("2026-07-28T02:00:00Z");

            UserResponse response = new UserResponse(
                    userId,
                    createdAt,
                    updatedAt,
                    "tester",
                    "tester@test.com",
                    profileId,
                    true
            );

            given(userService.read(userId))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/users/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(userId.toString()))
                    .andExpect(jsonPath("$.createdAt")
                            .value(createdAt.toString()))
                    .andExpect(jsonPath("$.updatedAt")
                            .value(updatedAt.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("tester"))
                    .andExpect(jsonPath("$.email")
                            .value("tester@test.com"))
                    .andExpect(jsonPath("$.profileId")
                            .value(profileId.toString()))
                    .andExpect(jsonPath("$.profileImageId")
                            .value(profileId.toString()))
                    .andExpect(jsonPath("$.online")
                            .value(true));

            then(userService)
                    .should()
                    .read(userId);
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 조회하면 400 Bad Request를 반환한다")
        void invalidUserId() throws Exception {
            // when & then
            mockMvc.perform(get(
                            "/api/users/{userId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());

            then(userService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    class ReadAll {

        @Test
        @DisplayName("전체 사용자 목록과 JSON 응답을 반환한다")
        void success() throws Exception {
            // given
            UUID firstUserId = UUID.randomUUID();
            UUID secondUserId = UUID.randomUUID();

            UserResponse firstUser = new UserResponse(
                    firstUserId,
                    Instant.parse("2026-07-28T01:00:00Z"),
                    null,
                    "user1",
                    "user1@test.com",
                    null,
                    true
            );

            UserResponse secondUser = new UserResponse(
                    secondUserId,
                    Instant.parse("2026-07-28T02:00:00Z"),
                    null,
                    "user2",
                    "user2@test.com",
                    null,
                    false
            );

            given(userService.readAll())
                    .willReturn(List.of(firstUser, secondUser));

            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()")
                            .value(2))
                    .andExpect(jsonPath("$[0].id")
                            .value(firstUserId.toString()))
                    .andExpect(jsonPath("$[0].username")
                            .value("user1"))
                    .andExpect(jsonPath("$[0].online")
                            .value(true))
                    .andExpect(jsonPath("$[1].id")
                            .value(secondUserId.toString()))
                    .andExpect(jsonPath("$[1].username")
                            .value("user2"))
                    .andExpect(jsonPath("$[1].online")
                            .value(false));

            then(userService)
                    .should()
                    .readAll();
        }

        @Test
        @DisplayName("사용자가 없으면 빈 JSON 배열을 반환한다")
        void empty() throws Exception {
            // given
            given(userService.readAll())
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            then(userService)
                    .should()
                    .readAll();
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class Delete {

        @Test
        @DisplayName("사용자를 삭제하면 204 No Content를 반환한다")
        void success() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete(
                            "/api/users/{userId}",
                            userId
                    ))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            then(userService)
                    .should()
                    .delete(userId);
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제하면 400 Bad Request를 반환한다")
        void invalidUserId() throws Exception {
            // when & then
            mockMvc.perform(delete(
                            "/api/users/{userId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());

            then(userService)
                    .shouldHaveNoInteractions();
        }
    }
}