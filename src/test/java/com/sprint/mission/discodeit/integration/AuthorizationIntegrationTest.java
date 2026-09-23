package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인가 통합 테스트")
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("인증 필요")
    class AuthenticationRequiredTest {

        @Test
        @DisplayName("인증되지 않은 사용자가 보호된 API에 접근하면 401을 반환한다")
        void should_ReturnUnauthorized_when_UnauthenticatedUserAccessesProtectedApi()
                throws Exception {

            mockMvc.perform(
                            get("/api/users")
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(401)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-001")
                    );
        }
    }

    @Nested
    @DisplayName("퍼블릭 채널 권한")
    class PublicChannelAuthorizationTest {

        @Test
        @DisplayName("USER 권한은 퍼블릭 채널을 생성할 수 없다")
        void should_ReturnForbidden_when_UserCreatesPublicChannel()
                throws Exception {

            createUser(
                    "normalUser",
                    "normal-user@test.com",
                    "password",
                    Role.USER
            );

            MockHttpSession session =
                    login(
                            "normalUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "name": "public-channel",
                        "description": "test channel"
                    }
                    """;

            mockMvc.perform(
                            post("/api/channels/public")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );
        }

        @Test
        @DisplayName("CHANNEL_MANAGER 권한은 퍼블릭 채널을 생성하면 201 Created를 반환한다")
        void should_CreatePublicChannel_when_ChannelManagerCreatesChannel()
                throws Exception {

            createUser(
                    "channelManager",
                    "channel-manager@test.com",
                    "password",
                    Role.CHANNEL_MANAGER
            );

            MockHttpSession session =
                    login(
                            "channelManager",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "name": "manager-channel",
                        "description": "manager test channel"
                    }
                    """;

            mockMvc.perform(
                            post("/api/channels/public")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isCreated()
                    )
                    .andExpect(
                            jsonPath("$.name")
                                    .value("manager-channel")
                    )
                    .andExpect(
                            jsonPath("$.type")
                                    .value("PUBLIC")
                    );
        }

        @Test
        @DisplayName("ADMIN 권한은 RoleHierarchy에 의해 퍼블릭 채널을 생성하면 201 Created를 반환한다")
        void should_CreatePublicChannel_when_AdminCreatesChannel()
                throws Exception {

            createUser(
                    "adminUser",
                    "admin-user@test.com",
                    "password",
                    Role.ADMIN
            );

            MockHttpSession session =
                    login(
                            "adminUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "name": "admin-channel",
                        "description": "admin test channel"
                    }
                    """;

            mockMvc.perform(
                            post("/api/channels/public")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isCreated()
                    )
                    .andExpect(
                            jsonPath("$.name")
                                    .value("admin-channel")
                    )
                    .andExpect(
                            jsonPath("$.type")
                                    .value("PUBLIC")
                    );
        }
    }

    @Nested
    @DisplayName("사용자 권한 수정")
    class UserRoleAuthorizationTest {

        @Test
        @DisplayName("USER 권한은 다른 사용자의 권한을 수정할 수 없다")
        void should_ReturnForbidden_when_UserUpdatesRole()
                throws Exception {

            createUser(
                    "normalRoleUser",
                    "normal-role@test.com",
                    "password",
                    Role.USER
            );

            UUID targetUserId =
                    createUser(
                            "targetUser1",
                            "target1@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "normalRoleUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(targetUserId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );
        }

        @Test
        @DisplayName("CHANNEL_MANAGER 권한은 다른 사용자의 권한을 수정할 수 없다")
        void should_ReturnForbidden_when_ChannelManagerUpdatesRole()
                throws Exception {

            createUser(
                    "managerRoleUser",
                    "manager-role@test.com",
                    "password",
                    Role.CHANNEL_MANAGER
            );

            UUID targetUserId =
                    createUser(
                            "targetUser2",
                            "target2@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "managerRoleUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "ADMIN"
                    }
                    """.formatted(targetUserId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );
        }

        @Test
        @DisplayName("ADMIN 권한은 다른 사용자의 권한을 수정할 수 있다")
        void should_UpdateRole_when_AdminUpdatesRole()
                throws Exception {

            createUser(
                    "roleAdmin",
                    "role-admin@test.com",
                    "password",
                    Role.ADMIN
            );

            UUID targetUserId =
                    createUser(
                            "targetUser3",
                            "target3@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "roleAdmin",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(targetUserId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(targetUserId.toString())
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value("CHANNEL_MANAGER")
                    );
        }
    }

    @Nested
    @DisplayName("사용자 리소스 권한")
    class UserResourceAuthorizationTest {

        @Test
        @DisplayName("사용자는 자신의 정보를 수정할 수 있다")
        void should_UpdateUser_when_UserUpdatesOwnInformation()
                throws Exception {

            UUID userId =
                    createUser(
                            "userOwner1",
                            "user-owner1@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "userOwner1",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "username": "userOwnerUpdated"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId.toString())
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("userOwnerUpdated")
                    );
        }

        @Test
        @DisplayName("사용자는 다른 사용자의 정보를 수정할 수 없다")
        void should_ReturnForbidden_when_UserUpdatesOtherUser()
                throws Exception {

            createUser(
                    "userAttacker1",
                    "user-attacker1@test.com",
                    "password",
                    Role.USER
            );

            UUID targetUserId =
                    createUser(
                            "userTarget1",
                            "user-target1@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "userAttacker1",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "username": "hackedUsername"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/users/{userId}",
                                    targetUserId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );

            User targetUser =
                    userRepository
                            .findById(targetUserId)
                            .orElseThrow();

            assertThat(
                    targetUser.getUsername()
            )
                    .isEqualTo("userTarget1");
        }

        @Test
        @DisplayName("사용자는 자신의 계정을 삭제할 수 있다")
        void should_DeleteUser_when_UserDeletesOwnAccount()
                throws Exception {

            UUID userId =
                    createUser(
                            "deleteOwner",
                            "delete-owner@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "deleteOwner",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            delete(
                                    "/api/users/{userId}",
                                    userId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isNoContent()
                    );

            assertThat(
                    userRepository.existsById(userId)
            )
                    .isFalse();
        }

        @Test
        @DisplayName("사용자는 다른 사용자의 계정을 삭제할 수 없다")
        void should_ReturnForbidden_when_UserDeletesOtherUser()
                throws Exception {

            createUser(
                    "deleteAttacker",
                    "delete-attacker@test.com",
                    "password",
                    Role.USER
            );

            UUID targetUserId =
                    createUser(
                            "deleteTarget",
                            "delete-target@test.com",
                            "password",
                            Role.USER
                    );

            MockHttpSession session =
                    login(
                            "deleteAttacker",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            delete(
                                    "/api/users/{userId}",
                                    targetUserId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );

            assertThat(
                    userRepository.existsById(targetUserId)
            )
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("메시지 리소스 권한")
    class MessageResourceAuthorizationTest {

        @Test
        @DisplayName("메시지 작성자는 자신의 메시지를 수정할 수 있다")
        void should_UpdateMessage_when_AuthorUpdatesOwnMessage()
                throws Exception {

            UUID authorId =
                    createUser(
                            "messageAuthor1",
                            "message-author1@test.com",
                            "password",
                            Role.USER
                    );

            UUID channelId =
                    createPublicChannel();

            UUID messageId =
                    createMessage(
                            authorId,
                            channelId,
                            "original message"
                    );

            MockHttpSession session =
                    login(
                            "messageAuthor1",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "content": "updated message"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/messages/{messageId}",
                                    messageId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(messageId.toString())
                    )
                    .andExpect(
                            jsonPath("$.content")
                                    .value("updated message")
                    );
        }

        @Test
        @DisplayName("다른 사용자는 작성자의 메시지를 수정할 수 없다")
        void should_ReturnForbidden_when_OtherUserUpdatesMessage()
                throws Exception {

            UUID authorId =
                    createUser(
                            "messageAuthor2",
                            "message-author2@test.com",
                            "password",
                            Role.USER
                    );

            createUser(
                    "messageAttacker2",
                    "message-attacker2@test.com",
                    "password",
                    Role.USER
            );

            UUID channelId =
                    createPublicChannel();

            UUID messageId =
                    createMessage(
                            authorId,
                            channelId,
                            "protected message"
                    );

            MockHttpSession session =
                    login(
                            "messageAttacker2",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "content": "hacked message"
                    }
                    """;

            mockMvc.perform(
                            patch(
                                    "/api/messages/{messageId}",
                                    messageId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );
        }

        @Test
        @DisplayName("메시지 작성자는 자신의 메시지를 삭제할 수 있다")
        void should_DeleteMessage_when_AuthorDeletesOwnMessage()
                throws Exception {

            UUID authorId =
                    createUser(
                            "messageAuthor3",
                            "message-author3@test.com",
                            "password",
                            Role.USER
                    );

            UUID channelId =
                    createPublicChannel();

            UUID messageId =
                    createMessage(
                            authorId,
                            channelId,
                            "delete my message"
                    );

            MockHttpSession session =
                    login(
                            "messageAuthor3",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            delete(
                                    "/api/messages/{messageId}",
                                    messageId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isNoContent()
                    );
        }

        @Test
        @DisplayName("다른 사용자는 작성자의 메시지를 삭제할 수 없다")
        void should_ReturnForbidden_when_OtherUserDeletesMessage()
                throws Exception {

            UUID authorId =
                    createUser(
                            "messageAuthor4",
                            "message-author4@test.com",
                            "password",
                            Role.USER
                    );

            createUser(
                    "messageAttacker4",
                    "message-attacker4@test.com",
                    "password",
                    Role.USER
            );

            UUID channelId =
                    createPublicChannel();

            UUID messageId =
                    createMessage(
                            authorId,
                            channelId,
                            "protected delete message"
                    );

            MockHttpSession session =
                    login(
                            "messageAttacker4",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            delete(
                                    "/api/messages/{messageId}",
                                    messageId
                            )
                                    .session(session)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(403)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-002")
                    );
        }
    }

    private UUID createUser(
            String username,
            String email,
            String password,
            Role role
    ) throws Exception {

        UserCreateRequest request =
                new UserCreateRequest(
                        username,
                        email,
                        password,
                        null
                );

        CsrfData csrfData =
                getCsrfData();

        MvcResult result =
                mockMvc.perform(
                                post("/api/users")
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
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

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        UUID userId =
                UUID.fromString(
                        body.get("id")
                                .asText()
                );

        if (role != Role.USER) {

            User user =
                    userRepository
                            .findById(userId)
                            .orElseThrow();

            user.updateRole(role);

            userRepository.saveAndFlush(
                    user
            );
        }

        User savedUser =
                userRepository
                        .findById(userId)
                        .orElseThrow();

        assertThat(
                savedUser.getRole()
        )
                .isEqualTo(role);

        return userId;
    }

    /*
     * 메시지 리소스 테스트에서 사용할
     * PUBLIC 채널을 생성한다.
     *
     * username 최대 길이 제한을 넘지 않도록
     * UUID 전체가 아니라 앞 8자리만 사용한다.
     */
    private UUID createPublicChannel()
            throws Exception {

        String randomValue =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String username =
                "manager-" + randomValue;

        String email =
                username + "@test.com";

        createUser(
                username,
                email,
                "password",
                Role.CHANNEL_MANAGER
        );

        MockHttpSession session =
                login(
                        username,
                        "password"
                );

        CsrfData csrfData =
                getCsrfData();

        String channelName =
                "channel-" + randomValue;

        String requestBody = """
                {
                    "name": "%s",
                    "description": "authorization test channel"
                }
                """.formatted(
                channelName
        );

        MvcResult result =
                mockMvc.perform(
                                post("/api/channels/public")
                                        .session(session)
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(requestBody)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return UUID.fromString(
                body.get("id")
                        .asText()
        );
    }

    private UUID createMessage(
            UUID authorId,
            UUID channelId,
            String content
    ) throws Exception {

        User author =
                userRepository
                        .findById(authorId)
                        .orElseThrow();

        MockHttpSession session =
                login(
                        author.getUsername(),
                        "password"
                );

        CsrfData csrfData =
                getCsrfData();

        String requestBody = """
                {
                    "content": "%s",
                    "authorId": "%s",
                    "channelId": "%s",
                    "attachments": []
                }
                """.formatted(
                content,
                authorId,
                channelId
        );

        MvcResult result =
                mockMvc.perform(
                                post("/api/messages")
                                        .session(session)
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(requestBody)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return UUID.fromString(
                body.get("id")
                        .asText()
        );
    }

    private MockHttpSession login(
            String username,
            String password
    ) throws Exception {

        CsrfData csrfData =
                getCsrfData();

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_FORM_URLENCODED
                                        )
                                        .param(
                                                "username",
                                                username
                                        )
                                        .param(
                                                "password",
                                                password
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertThat(session)
                .isNotNull();

        return session;
    }

    private CsrfData getCsrfData()
            throws Exception {

        MvcResult result =
                mockMvc.perform(
                                get("/api/auth/csrf-token")
                        )
                        .andExpect(
                                status()
                                        .isNonAuthoritativeInformation()
                        )
                        .andReturn();

        Cookie csrfCookie =
                result.getResponse()
                        .getCookie("XSRF-TOKEN");

        assertThat(csrfCookie)
                .isNotNull();

        assertThat(csrfCookie.getValue())
                .isNotBlank();

        return new CsrfData(
                csrfCookie,
                csrfCookie.getValue()
        );
    }

    private record CsrfData(
            Cookie cookie,
            String token
    ) {
    }
}