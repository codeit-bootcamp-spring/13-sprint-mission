package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 API 통합 테스트")
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiscodeitUserDetailsService
            discodeitUserDetailsService;

    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("사용자 생성")
    class Create {

        @Test
        @DisplayName("유효한 요청이면 사용자를 생성하고 200 OK를 반환한다")
        void success() throws Exception {

            UserCreateRequest request =
                    new UserCreateRequest(
                            "tester",
                            "tester@test.com",
                            "password",
                            null
                    );

            MvcResult result =
                    mockMvc.perform(
                                    post("/api/users")
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .content(
                                                    objectMapper
                                                            .writeValueAsString(
                                                                    request
                                                            )
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andExpect(
                                    content()
                                            .contentTypeCompatibleWith(
                                                    MediaType.APPLICATION_JSON
                                            )
                            )
                            .andExpect(
                                    jsonPath("$.id")
                                            .isNotEmpty()
                            )
                            .andExpect(
                                    jsonPath("$.username")
                                            .value("tester")
                            )
                            .andExpect(
                                    jsonPath("$.email")
                                            .value(
                                                    "tester@test.com"
                                            )
                            )
                            .andExpect(
                                    jsonPath("$.online")
                                            .value(false)
                            )
                            .andReturn();

            String responseBody =
                    result.getResponse()
                            .getContentAsString();

            String userId =
                    objectMapper
                            .readTree(responseBody)
                            .get("id")
                            .asText();

            assertThat(
                    UUID.fromString(userId)
            )
                    .isNotNull();

            mockMvc.perform(
                            get(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId)
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("tester")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value(
                                            "tester@test.com"
                                    )
                    );
        }

        @Test
        @DisplayName("사용자 이름이 비어 있으면 400 Bad Request를 반환한다")
        void blankUsername() throws Exception {

            UserCreateRequest request =
                    new UserCreateRequest(
                            "",
                            "tester@test.com",
                            "password",
                            null
                    );

            mockMvc.perform(
                            post("/api/users")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            objectMapper
                                                    .writeValueAsString(
                                                            request
                                                    )
                                    )
                    )
                    .andExpect(
                            status().isBadRequest()
                    );
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
        void invalidEmail() throws Exception {

            UserCreateRequest request =
                    new UserCreateRequest(
                            "tester",
                            "invalid-email",
                            "password",
                            null
                    );

            mockMvc.perform(
                            post("/api/users")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            objectMapper
                                                    .writeValueAsString(
                                                            request
                                                    )
                                    )
                    )
                    .andExpect(
                            status().isBadRequest()
                    );
        }

        @Test
        @DisplayName("동일한 이메일로 사용자를 생성하면 실패한다")
        void duplicatedEmail() throws Exception {

            UserCreateRequest firstRequest =
                    new UserCreateRequest(
                            "tester1",
                            "duplicate@test.com",
                            "password",
                            null
                    );

            UserCreateRequest secondRequest =
                    new UserCreateRequest(
                            "tester2",
                            "duplicate@test.com",
                            "password",
                            null
                    );

            mockMvc.perform(
                            post("/api/users")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            objectMapper
                                                    .writeValueAsString(
                                                            firstRequest
                                                    )
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    );

            mockMvc.perform(
                            post("/api/users")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            objectMapper
                                                    .writeValueAsString(
                                                            secondRequest
                                                    )
                                    )
                    )
                    .andExpect(
                            status().is4xxClientError()
                    );
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    class ReadAll {

        @Test
        @DisplayName("생성한 사용자들이 전체 목록에 포함된다")
        void success() throws Exception {

            createUser(
                    "user1",
                    "user1@test.com",
                    "password"
            );

            createUser(
                    "user2",
                    "user2@test.com",
                    "password"
            );

            mockMvc.perform(
                            get("/api/users")
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            content()
                                    .contentTypeCompatibleWith(
                                            MediaType.APPLICATION_JSON
                                    )
                    )
                    .andExpect(
                            jsonPath("$")
                                    .isArray()
                    )
                    .andExpect(
                            jsonPath("$.length()")
                                    .value(2)
                    )
                    .andExpect(
                            jsonPath("$[0].id")
                                    .isNotEmpty()
                    )
                    .andExpect(
                            jsonPath("$[1].id")
                                    .isNotEmpty()
                    )
                    .andExpect(
                            jsonPath("$[*].username")
                                    .value(
                                            org.hamcrest.Matchers
                                                    .containsInAnyOrder(
                                                            "user1",
                                                            "user2"
                                                    )
                                    )
                    )
                    .andExpect(
                            jsonPath("$[*].email")
                                    .value(
                                            org.hamcrest.Matchers
                                                    .containsInAnyOrder(
                                                            "user1@test.com",
                                                            "user2@test.com"
                                                    )
                                    )
                    );
        }

        @Test
        @DisplayName("사용자가 없으면 빈 JSON 배열을 반환한다")
        void empty() throws Exception {

            mockMvc.perform(
                            get("/api/users")
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$")
                                    .isArray()
                    )
                    .andExpect(
                            jsonPath("$")
                                    .isEmpty()
                    );
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class Update {

        @Test
        @DisplayName("생성한 사용자를 수정하면 변경된 사용자 정보를 반환한다")
        void success() throws Exception {

            String userId =
                    createUser(
                            "beforeUser",
                            "before@test.com",
                            "password"
                    );

            /*
             * Method Security의 본인 확인을 통과하도록
             * 수정 대상 사용자로 인증한다.
             */
            authenticateAs(
                    "beforeUser"
            );

            String requestJson = """
                    {
                      "newUsername": "afterUser",
                      "newEmail": "after@test.com",
                      "newPassword": "newPassword"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestJson)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            content()
                                    .contentTypeCompatibleWith(
                                            MediaType.APPLICATION_JSON
                                    )
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId)
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("afterUser")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("after@test.com")
                    )
                    .andExpect(
                            jsonPath("$.updatedAt")
                                    .isNotEmpty()
                    );

            mockMvc.perform(
                            get(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId)
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("afterUser")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("after@test.com")
                    );
        }

        @Test
        @DisplayName("수정 이메일 형식이 올바르지 않으면 400 Bad Request를 반환한다")
        void invalidEmail() throws Exception {

            String userId =
                    createUser(
                            "tester",
                            "tester@test.com",
                            "password"
                    );

            String requestJson = """
                    {
                      "newUsername": "updatedUser",
                      "newEmail": "invalid-email",
                      "newPassword": "newPassword"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestJson)
                    )
                    .andExpect(
                            status().isBadRequest()
                    );

            mockMvc.perform(
                            get(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("tester")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("tester@test.com")
                    );
        }

        @Test
        @DisplayName("수정 사용자 이름이 2자 미만이면 400 Bad Request를 반환한다")
        void usernameTooShort() throws Exception {

            String userId =
                    createUser(
                            "tester",
                            "tester@test.com",
                            "password"
                    );

            String requestJson = """
                    {
                      "newUsername": "a",
                      "newEmail": "updated@test.com",
                      "newPassword": "newPassword"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestJson)
                    )
                    .andExpect(
                            status().isBadRequest()
                    );

            mockMvc.perform(
                            get(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("tester")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("tester@test.com")
                    );
        }

        @Test
        @DisplayName("수정 비밀번호가 4자 미만이면 400 Bad Request를 반환한다")
        void passwordTooShort() throws Exception {

            String userId =
                    createUser(
                            "tester",
                            "tester@test.com",
                            "password"
                    );

            String requestJson = """
                    {
                      "newUsername": "updatedUser",
                      "newEmail": "updated@test.com",
                      "newPassword": "123"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestJson)
                    )
                    .andExpect(
                            status().isBadRequest()
                    );

            mockMvc.perform(
                            get(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("tester")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("tester@test.com")
                    );
        }

        @Test
        @DisplayName("잘못된 사용자 UUID 형식으로 수정하면 400 Bad Request를 반환한다")
        void invalidUserId() throws Exception {

            String requestJson = """
                    {
                      "newUsername": "updatedUser",
                      "newEmail": "updated@test.com",
                      "newPassword": "newPassword"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    "invalid-uuid"
                            )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestJson)
                    )
                    .andExpect(
                            status().isBadRequest()
                    );
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class Delete {

        @Test
        @DisplayName("생성한 사용자를 삭제하면 204 No Content를 반환한다")
        void success() throws Exception {

            String userId =
                    createUser(
                            "deleteUser",
                            "delete@test.com",
                            "password"
                    );

            /*
             * 본인 계정 삭제이므로
             * 삭제 대상 사용자로 인증한다.
             */
            authenticateAs(
                    "deleteUser"
            );

            mockMvc.perform(
                            delete(
                                    "/api/users/{userId}",
                                    userId
                            )
                    )
                    .andExpect(
                            status().isNoContent()
                    )
                    .andExpect(
                            content().string("")
                    );

            mockMvc.perform(
                            get("/api/users")
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$")
                                    .isArray()
                    )
                    .andExpect(
                            jsonPath("$")
                                    .isEmpty()
                    );
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제하면 400 Bad Request를 반환한다")
        void invalidUserId() throws Exception {

            mockMvc.perform(
                            delete(
                                    "/api/users/{userId}",
                                    "invalid-uuid"
                            )
                    )
                    .andExpect(
                            status().isBadRequest()
                    );
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 404 Not Found를 반환한다")
        void userNotFound() throws Exception {

            // given
            createUser(
                    "notFoundRequester",
                    "not-found-requester@test.com",
                    "password"
            );

            /*
             * 보호된 API이므로 인증된 사용자 상태에서
             * 존재하지 않는 사용자 ID를 요청한다.
             */
            authenticateAs(
                    "notFoundRequester"
            );

            UUID unknownUserId =
                    UUID.randomUUID();

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/users/{userId}",
                                    unknownUserId
                            )
                    )
                    .andExpect(
                            status().isNotFound()
                    );
        }
    }

    private String createUser(
            String username,
            String email,
            String password
    ) throws Exception {

        UserCreateRequest request =
                new UserCreateRequest(
                        username,
                        email,
                        password,
                        null
                );

        MvcResult result =
                mockMvc.perform(
                                post("/api/users")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper
                                                        .writeValueAsString(
                                                                request
                                                        )
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        return objectMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asText();
    }

    /*
     * 실제 프로젝트의 DiscodeitUserDetails를 이용해
     * Method Security가 인식할 Authentication을 만든다.
     */
    private void authenticateAs(
            String username
    ) {

        UserDetails userDetails =
                discodeitUserDetailsService
                        .loadUserByUsername(
                                username
                        );

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken
                        .authenticated(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}