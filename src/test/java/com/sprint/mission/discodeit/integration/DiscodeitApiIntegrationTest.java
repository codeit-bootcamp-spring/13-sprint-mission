package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.request.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Discodeit API 통합 테스트")
class DiscodeitApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageFileRepository messageFileRepository;

    @Autowired
    BinaryContentRepository binaryContentRepository;

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Autowired
    EntityManager em;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SessionRegistry sessionRegistry;

    @Autowired
    HttpSessionEventPublisher httpSessionEventPublisher;

    @Autowired
    WebApplicationContext webApplicationContext;

    @AfterEach
    void clearSessionRegistry() {
        sessionRegistry.getAllPrincipals().forEach(principal ->
                sessionRegistry.getAllSessions(principal, true).forEach(sessionInformation ->
                        sessionRegistry.removeSessionInformation(sessionInformation.getSessionId())
                )
        );
    }

    @Test
    @DisplayName("사용자 생성 및 로그인 통합 성공 - 실제 DB 사용자로 인증")
    void userCreateAndLogin_authenticatesPersistedUser() throws Exception {
        // given
        // 통합 테스트는 Controller 슬라이스 테스트와 달리 Service, Repository, Mapper, DB를 모두 실제 Bean으로 사용한다.
        // 이 테스트는 사용자 생성 요청이 User와 BinaryContent 저장까지 이어지고,
        // 이후 저장된 계정으로 로그인할 수 있는지 확인한다.
        String suffix = uniqueSuffix();
        UserCreateRequest createRequest = new UserCreateRequest(
                "integrationUser-" + suffix,
                "integrationPassword",
                "integration-" + suffix + "@gmail.com"
        );
        MockMultipartFile userCreateRequestPart = jsonPart("userCreateRequest", createRequest);
        MockMultipartFile profilePart = filePart(
                "profile",
                "profile-" + suffix + ".png",
                MediaType.IMAGE_PNG_VALUE,
                "profile-image"
        );

        // when
        // 실제 multipart 사용자 생성 API를 호출한다.
        MvcResult createResult = mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequestPart)
                        .file(profilePart)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // HTTP 응답은 200 OK이며, 생성된 사용자와 프로필 메타데이터를 JSON으로 내려줘야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(createRequest.username()))
                .andExpect(jsonPath("$.email").value(createRequest.email()))
                .andExpect(jsonPath("$.profile.id").exists())
                .andExpect(jsonPath("$.profile.fileName").value(profilePart.getOriginalFilename()))
                .andExpect(jsonPath("$.profile.size").value(profilePart.getSize()))
                .andExpect(jsonPath("$.profile.contentType").value(profilePart.getContentType()))
                .andExpect(jsonPath("$.online").value(false))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andReturn();

        JsonNode createBody = readBody(createResult);
        UUID userId = uuidAt(createBody, "/id");
        UUID profileId = uuidAt(createBody, "/profile/id");

        // 실제 DB 저장 여부를 Repository 재조회로 확인한다.
        // 영속성 컨텍스트를 비워 API 호출 결과가 1차 캐시가 아니라 DB에 flush된 상태인지 확인한다.
        flushAndClear();
        User savedUser = userRepository.findById(userId).orElseThrow(AssertionError::new);

        assertThat(savedUser.getUsername()).isEqualTo(createRequest.username());
        assertThat(savedUser.getPassword()).isNotEqualTo(createRequest.password());
        assertThat(passwordEncoder.matches(createRequest.password(), savedUser.getPassword())).isTrue();
        assertThat(savedUser.getEmail()).isEqualTo(createRequest.email());
        assertThat(savedUser.getProfileId()).isEqualTo(profileId);
        assertThat(binaryContentRepository.findById(profileId)).isPresent();

        // when
        // 생성한 사용자 계정으로 실제 로그인 API를 호출한다.
        MvcResult loginResult = performLogin(createRequest.username(), createRequest.password())

                // then
                // 폼 로그인 필터와 커스텀 인증 컴포넌트가 함께 동작해 사용자 정보를 내려줘야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(createRequest.username()))
                .andExpect(jsonPath("$.online").value(true))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(authenticated().withUsername(createRequest.username()))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
    }

    @Test
    @DisplayName("현재 사용자 조회 성공 - 로그인 세션으로 동일한 사용자 정보 반환")
    void getMe_returnsCurrentUser_whenSessionIsAuthenticated() throws Exception {
        // given
        // 실제 사용자 생성 및 폼 로그인을 통해 SecurityContext가 저장된 HTTP 세션을 준비한다.
        String suffix = uniqueSuffix();
        String username = "sessionUser-" + suffix;
        String email = "session-user-" + suffix + "@gmail.com";
        UUID userId = createUser(username, email);

        // 실제로 분리된 HTTP 요청처럼 로그인에서 사용자와 상태를 DB로부터 다시 조회하도록 한다.
        flushAndClear();

        MvcResult loginResult = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // when & then
        // 브라우저가 JSESSIONID 쿠키를 자동으로 전달하는 동작을 동일한 MockHttpSession 재사용으로 검증한다.
        mockMvc.perform(get("/api/auth/me")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(authenticated().withUsername(username))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.online").value(true));
    }

    @Test
    @DisplayName("로그아웃 성공 - 인증과 세션을 제거하고 204 응답 반환")
    void logout_invalidatesSessionAndReturnsNoContent() throws Exception {
        // given
        // 실제 사용자 생성 및 폼 로그인을 통해 SecurityContext가 저장된 HTTP 세션을 준비한다.
        String suffix = uniqueSuffix();
        String username = "logoutUser-" + suffix;
        createUser(username, "logout-user-" + suffix + "@gmail.com");

        // 실제로 분리된 HTTP 요청처럼 로그인에서 사용자를 DB로부터 다시 조회하도록 한다.
        flushAndClear();

        MvcResult loginResult = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(sessionRegistry.getSessionInformation(session.getId())).isNotNull();

        // when & then
        // 동일한 세션과 CSRF 토큰으로 로그아웃하면 인증 및 세션과 JSESSIONID 쿠키가 제거되어야 한다.
        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(unauthenticated())
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("HTTP 세션 만료 이벤트가 발생하면 세션 레지스트리에서도 제거한다")
    void sessionDestroyedEvent_removesSessionFromRegistry() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                webApplicationContext
        );
        MockHttpSession session = new MockHttpSession(servletContext);
        String principal = "session-event-user";
        sessionRegistry.registerNewSession(session.getId(), principal);
        assertThat(sessionRegistry.getSessionInformation(session.getId())).isNotNull();

        httpSessionEventPublisher.sessionDestroyed(new jakarta.servlet.http.HttpSessionEvent(session));

        assertThat(sessionRegistry.getSessionInformation(session.getId())).isNull();
    }

    @Test
    @DisplayName("동일한 계정으로 다시 로그인하면 기존 세션을 만료하고 새 세션을 유지한다")
    void login_expiresPreviousSession_whenSameAccountLogsInAgain() throws Exception {
        String suffix = uniqueSuffix();
        String username = "concurrentUser-" + suffix;
        UUID userId = createUser(username, "concurrent-user-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult firstLogin = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession firstSession = (MockHttpSession) firstLogin.getRequest().getSession(false);
        assertThat(firstSession).isNotNull();

        MvcResult secondLogin = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession secondSession = (MockHttpSession) secondLogin.getRequest().getSession(false);
        assertThat(secondSession).isNotNull();
        assertThat(secondSession.getId()).isNotEqualTo(firstSession.getId());

        SessionInformation firstSessionInformation =
                sessionRegistry.getSessionInformation(firstSession.getId());
        SessionInformation secondSessionInformation =
                sessionRegistry.getSessionInformation(secondSession.getId());

        assertThat(firstSessionInformation).isNotNull();
        assertThat(firstSessionInformation.isExpired()).isTrue();
        assertThat(secondSessionInformation).isNotNull();
        assertThat(secondSessionInformation.isExpired()).isFalse();

        mockMvc.perform(get("/api/auth/me").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.online").value(true));
    }

    @Test
    @DisplayName("로그인 유지 성공 - 세션 쿠키 없이 Remember-Me 쿠키로 자동 로그인")
    void rememberMe_autoLogsIn_whenSessionCookieIsMissing() throws Exception {
        String suffix = uniqueSuffix();
        String username = "rememberMeUser-" + suffix;
        UUID userId = createUser(username, "remember-me-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult loginResult = performRememberMeLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andExpect(cookie().exists("remember-me"))
                .andExpect(cookie().maxAge("remember-me", 60 * 60 * 24 * 30))
                .andReturn();

        Cookie rememberMeCookie = loginResult.getResponse().getCookie("remember-me");
        MockHttpSession loginSession = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(rememberMeCookie).isNotNull();
        assertThat(loginSession).isNotNull();

        MvcResult autoLoginResult = mockMvc.perform(get("/api/auth/me")
                        .cookie(rememberMeCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.online").value(true))
                .andReturn();

        MockHttpSession autoLoginSession =
                (MockHttpSession) autoLoginResult.getRequest().getSession(false);
        assertThat(autoLoginSession).isNotNull();
        assertThat(autoLoginSession.getId()).isNotEqualTo(loginSession.getId());
    }

    @Test
    @DisplayName("로그인 유지 미선택 - Remember-Me 쿠키를 발급하지 않는다")
    void rememberMe_doesNotIssueCookie_whenParameterIsMissing() throws Exception {
        String suffix = uniqueSuffix();
        String username = "noRememberMeUser-" + suffix;
        createUser(username, "no-remember-me-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult loginResult = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andExpect(cookie().doesNotExist("remember-me"))
                .andReturn();

        assertThat(loginResult.getResponse().getCookie("remember-me")).isNull();
    }

    @Test
    @DisplayName("로그인 유지 해제 - remember-me가 false이면 쿠키를 발급하지 않는다")
    void rememberMe_doesNotIssueCookie_whenParameterIsFalse() throws Exception {
        String suffix = uniqueSuffix();
        String username = "falseRememberMeUser-" + suffix;
        createUser(username, "false-remember-me-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult loginResult = performLoginWithRememberMe(
                        username,
                        "integrationPassword",
                        false
                )
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andExpect(cookie().doesNotExist("remember-me"))
                .andReturn();

        assertThat(loginResult.getResponse().getCookie("remember-me")).isNull();
    }

    @Test
    @DisplayName("로그인 유지 로그아웃 성공 - 세션과 Remember-Me 쿠키를 제거한다")
    void rememberMe_logoutInvalidatesSessionAndCookie() throws Exception {
        String suffix = uniqueSuffix();
        String username = "rememberMeLogoutUser-" + suffix;
        createUser(username, "remember-me-logout-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult loginResult = performRememberMeLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andReturn();
        Cookie rememberMeCookie = loginResult.getResponse().getCookie("remember-me");
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(rememberMeCookie).isNotNull();
        assertThat(session).isNotNull();

        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .cookie(rememberMeCookie)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(unauthenticated())
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(cookie().maxAge("remember-me", 0));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("사용자 역할을 변경하면 대상 사용자의 세션만 만료한다")
    void updateUserRole_expiresOnlyTargetUserSession() throws Exception {
        String suffix = uniqueSuffix();
        String targetUsername = "roleSessionTarget-" + suffix;
        String otherUsername = "roleSessionOther-" + suffix;
        UUID targetUserId = createUser(
                targetUsername,
                "role-session-target-" + suffix + "@gmail.com"
        );
        createUser(otherUsername, "role-session-other-" + suffix + "@gmail.com");
        flushAndClear();

        MvcResult targetLogin = performLogin(targetUsername, "integrationPassword")
                .andExpect(status().isOk())
                .andReturn();
        MvcResult otherLogin = performLogin(otherUsername, "integrationPassword")
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession targetSession = (MockHttpSession) targetLogin.getRequest().getSession(false);
        MockHttpSession otherSession = (MockHttpSession) otherLogin.getRequest().getSession(false);
        assertThat(targetSession).isNotNull();
        assertThat(otherSession).isNotNull();

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(
                targetUserId,
                Role.CHANNEL_MANAGER
        );
        mockMvc.perform(put("/api/auth/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUserId.toString()))
                .andExpect(jsonPath("$.role").value(Role.CHANNEL_MANAGER.name()))
                .andExpect(jsonPath("$.online").value(false));

        SessionInformation targetSessionInformation =
                sessionRegistry.getSessionInformation(targetSession.getId());
        SessionInformation otherSessionInformation =
                sessionRegistry.getSessionInformation(otherSession.getId());

        assertThat(targetSessionInformation).isNotNull();
        assertThat(targetSessionInformation.isExpired()).isTrue();
        assertThat(otherSessionInformation).isNotNull();
        assertThat(otherSessionInformation.isExpired()).isFalse();
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 표준 401 오류 응답 반환")
    void login_returnsUnauthorized_whenPasswordIsIncorrect() throws Exception {
        String suffix = uniqueSuffix();
        String username = "wrongPasswordUser-" + suffix;
        createUser(username, "wrong-password-" + suffix + "@gmail.com");

        performLogin(username, "incorrectPassword")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.exceptionType").value("UserLoginFailedException"))
                .andExpect(jsonPath("$.code").value("USER_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자가 존재하지 않으면 표준 401 오류 응답 반환")
    void login_returnsUnauthorized_whenUserDoesNotExist() throws Exception {
        performLogin("unknownUser-" + uniqueSuffix(), "integrationPassword")
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.exceptionType").value("UserLoginFailedException"))
                .andExpect(jsonPath("$.code").value("USER_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("공개 채널, 메시지, 첨부 파일 통합 성공 - 메시지 저장 후 목록 조회와 다운로드 가능")
    void publicChannelMessageAndAttachment_flowPersistsAndDownloadsAttachment() throws Exception {
        // given
        // 실제 사용자와 공개 채널을 API로 만든 뒤, 그 사용자와 채널을 참조하는 메시지를 첨부 파일과 함께 생성한다.
        // 이 흐름은 Controller, Service, Repository, Mapper, Querydsl 목록 조회, 로컬 파일 스토리지를 함께 검증한다.
        String suffix = uniqueSuffix();
        UUID authorId = createUser("messageAuthor-" + suffix, "message-author-" + suffix + "@gmail.com");

        PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest(
                "public-channel-" + suffix,
                "integration public channel"
        );
        MvcResult channelCreateResult = mockMvc.perform(post("/api/channels/public")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channelCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
                .andExpect(jsonPath("$.name").value(channelCreateRequest.name()))
                .andReturn();

        UUID channelId = uuidAt(readBody(channelCreateResult), "/id");

        MessageCreateRequest messageCreateRequest = new MessageCreateRequest(
                "integration message",
                channelId,
                authorId
        );
        MockMultipartFile messageCreateRequestPart = jsonPart("messageCreateRequest", messageCreateRequest);
        MockMultipartFile attachmentPart = filePart(
                "attachments",
                "message-attachment-" + suffix + ".txt",
                MediaType.TEXT_PLAIN_VALUE,
                "attachment-content"
        );

        // when
        // 실제 메시지 생성 API를 multipart/form-data로 호출한다.
        MvcResult messageCreateResult = mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequestPart)
                        .file(attachmentPart)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 메시지 응답에는 작성자, 채널, 첨부 파일 메타데이터가 함께 포함되어야 한다.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value(messageCreateRequest.content()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.attachments.length()").value(1))
                .andExpect(jsonPath("$.attachments[0].id").exists())
                .andExpect(jsonPath("$.attachments[0].fileName").value(attachmentPart.getOriginalFilename()))
                .andExpect(jsonPath("$.attachments[0].size").value(attachmentPart.getSize()))
                .andExpect(jsonPath("$.attachments[0].contentType").value(attachmentPart.getContentType()))
                .andReturn();

        JsonNode messageCreateBody = readBody(messageCreateResult);
        UUID messageId = uuidAt(messageCreateBody, "/id");
        UUID attachmentId = uuidAt(messageCreateBody, "/attachments/0/id");

        // 실제 DB에 Message, MessageFile, BinaryContent가 저장됐는지 확인한다.
        flushAndClear();
        Channel savedChannel = channelRepository.findById(channelId).orElseThrow(AssertionError::new);
        Message savedMessage = messageRepository.findById(messageId).orElseThrow(AssertionError::new);
        List<MessageFile> savedMessageFiles = messageFileRepository.findAllByMessage_Id(messageId);

        assertThat(savedChannel.getType()).isEqualTo(ChannelType.PUBLIC);
        assertThat(savedMessage.getContent()).isEqualTo(messageCreateRequest.content());
        assertThat(savedMessage.getChannelId()).isEqualTo(channelId);
        assertThat(savedMessage.getAuthor().getId()).isEqualTo(authorId);
        assertThat(savedMessageFiles).hasSize(1);
        assertThat(savedMessageFiles.get(0).getFileId()).isEqualTo(attachmentId);
        assertThat(binaryContentRepository.findById(attachmentId)).isPresent();

        // when
        // 채널별 메시지 목록 API를 호출해 방금 만든 메시지가 Querydsl 목록 조회 경로로 조회되는지 확인한다.
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc")
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 목록 응답은 PageResponse 형태이며, 첨부 파일까지 함께 내려와야 한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$.content[0].content").value(messageCreateRequest.content()))
                .andExpect(jsonPath("$.content[0].attachments[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.hasNext").value(false));

        // when
        // 첨부 파일 다운로드 API를 호출한다.
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", attachmentId))

                // then
                // BinaryContentService와 LocalBinaryContentStorage가 실제 저장 파일을 읽어 응답해야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().longValue("Content-Length", attachmentPart.getSize()))
                .andExpect(content().bytes(attachmentPart.getBytes()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("비공개 채널과 읽음 상태 통합 성공 - 참여자별 ReadStatus 생성 후 수정 가능")
    void privateChannelAndReadStatus_flowCreatesAndUpdatesParticipantReadStatuses() throws Exception {
        // given
        // 비공개 채널 생성은 Channel 저장뿐 아니라 참여자별 ReadStatus bulk insert까지 수행한다.
        // 두 사용자를 실제 API로 만든 뒤 PRIVATE 채널 생성 API를 호출한다.
        String suffix = uniqueSuffix();
        UUID firstUserId = createUser("privateUserA-" + suffix, "private-a-" + suffix + "@gmail.com");
        UUID secondUserId = createUser("privateUserB-" + suffix, "private-b-" + suffix + "@gmail.com");

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(firstUserId, secondUserId)
        );

        // when
        // POST /api/channels/private 요청을 전송한다.
        MvcResult createResult = mockMvc.perform(post("/api/channels/private")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답은 PRIVATE 채널과 참여자 목록을 포함해야 한다.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value(ChannelType.PRIVATE.name()))
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andReturn();

        UUID channelId = uuidAt(readBody(createResult), "/id");

        // 실제 DB에서 채널과 참여자별 ReadStatus가 생성됐는지 확인한다.
        flushAndClear();
        Channel savedChannel = channelRepository.findById(channelId).orElseThrow(AssertionError::new);
        List<ReadStatus> channelReadStatuses = readStatusRepository.findByChannelId(channelId);

        assertThat(savedChannel.getType()).isEqualTo(ChannelType.PRIVATE);
        assertThat(channelReadStatuses)
                .extracting(ReadStatus::getUserId)
                .containsExactlyInAnyOrder(firstUserId, secondUserId);

        // when
        // 특정 사용자의 읽음 상태 목록을 조회한다.
        MvcResult readStatusListResult = mockMvc.perform(get("/api/readStatuses")
                        .param("userId", firstUserId.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 첫 번째 사용자에게 PRIVATE 채널의 ReadStatus가 조회되어야 한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(firstUserId.toString()))
                .andExpect(jsonPath("$[0].channelId").value(channelId.toString()))
                .andReturn();

        UUID readStatusId = uuidAt(readBody(readStatusListResult), "/0/id");

        // when
        // 읽음 상태의 lastReadAt을 실제 API로 수정한다.
        Instant newLastReadAt = Instant.parse("2026-07-28T02:30:30Z");
        ReadStatusUpdateRequest updateRequest = new ReadStatusUpdateRequest(newLastReadAt);
        mockMvc.perform(patch("/api/readStatuses/{readStatusId}", readStatusId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))

                // then
                // 수정 API 응답은 같은 ReadStatus id와 channelId를 유지해야 한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(readStatusId.toString()))
                .andExpect(jsonPath("$.userId").value(firstUserId.toString()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()));

        // DB에서도 lastReadAt이 요청값으로 갱신됐는지 확인한다.
        flushAndClear();
        ReadStatus updatedReadStatus = readStatusRepository.findById(readStatusId).orElseThrow(AssertionError::new);
        assertThat(updatedReadStatus.getLastReadAt()).isEqualTo(newLastReadAt);

        // when
        // 사용자별 채널 목록 API를 호출한다.
        mockMvc.perform(get("/api/channels")
                        .param("userId", firstUserId.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // PRIVATE 채널은 참여자인 사용자에게 visible channel로 조회되어야 한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(channelId.toString()))
                .andExpect(jsonPath("$[0].type").value(ChannelType.PRIVATE.name()));
    }

    @Test
    @DisplayName("사용자 목록, 수정, 삭제 통합 성공 - 실제 DB에 변경사항 반영")
    void userListUpdateAndDelete_flowPersistsUpdatesAndRemovesUser() throws Exception {
        // given
        // 사용자 관련 주요 API 중 기존 생성 흐름에서 다루지 않은 목록 조회, 수정, 삭제를 한 흐름에서 검증한다.
        // 모든 호출은 실제 Controller, Service, Repository, DB를 거친다.
        String suffix = uniqueSuffix();
        String firstUsername = "userListA-" + suffix;
        UUID firstUserId = createUser(firstUsername, "user-list-a-" + suffix + "@gmail.com");
        UUID secondUserId = createUser("userListB-" + suffix, "user-list-b-" + suffix + "@gmail.com");
        flushAndClear();
        MockHttpSession firstUserSession = loginSession(firstUsername);

        // when
        // 사용자 목록 API를 호출한다.
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .session(firstUserSession)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 방금 생성한 두 사용자가 실제 목록 응답에 포함되어야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        JsonNode listBody = readBody(listResult);
        assertThat(listBody.findValuesAsText("id"))
                .contains(firstUserId.toString(), secondUserId.toString());

        // when
        // 첫 번째 사용자의 username, password, email, profile을 multipart PATCH로 수정한다.
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "updatedUser-" + suffix,
                "updatedPassword",
                "updated-user-" + suffix + "@gmail.com"
        );
        MockMultipartFile updateRequestPart = jsonPart("userUpdateRequest", updateRequest);
        MockMultipartFile profilePart = filePart(
                "profile",
                "updated-profile-" + suffix + ".png",
                MediaType.IMAGE_PNG_VALUE,
                "updated-profile"
        );

        MvcResult updateResult = mockMvc.perform(multipart("/api/users/{userId}", firstUserId)
                        .file(updateRequestPart)
                        .file(profilePart)
                        .session(firstUserSession)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstUserId.toString()))
                .andExpect(jsonPath("$.username").value(updateRequest.newUsername()))
                .andExpect(jsonPath("$.email").value(updateRequest.newEmail()))
                .andExpect(jsonPath("$.profile.id").exists())
                .andExpect(jsonPath("$.profile.fileName").value(profilePart.getOriginalFilename()))
                .andReturn();

        UUID updatedProfileId = uuidAt(readBody(updateResult), "/profile/id");

        flushAndClear();
        User updatedUser = userRepository.findById(firstUserId).orElseThrow(AssertionError::new);
        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.newUsername());
        assertThat(updatedUser.getPassword()).isNotEqualTo(updateRequest.newPassword());
        assertThat(passwordEncoder.matches(updateRequest.newPassword(), updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(updatedUser.getProfileId()).isEqualTo(updatedProfileId);
        assertThat(binaryContentRepository.findById(updatedProfileId)).isPresent();

        // when
        // 수정한 사용자를 삭제한다.
        mockMvc.perform(delete("/api/users/{userId}", firstUserId)
                        .session(firstUserSession)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // then
        // User가 실제 DB에서 제거되고, 대조군 사용자는 남아 있어야 한다.
        flushAndClear();
        assertThat(userRepository.findById(firstUserId)).isEmpty();
        assertThat(userRepository.findById(secondUserId)).isPresent();
    }

    @Test
    @DisplayName("사용자 수정, 삭제 인가 실패 - 다른 사용자의 정보는 변경할 수 없다")
    void userUpdateAndDelete_returnsForbidden_whenRequesterIsNotOwner() throws Exception {
        String suffix = uniqueSuffix();
        String targetUsername = "userTarget-" + suffix;
        String targetEmail = "user-target-" + suffix + "@gmail.com";
        UUID targetUserId = createUser(targetUsername, targetEmail);
        String requesterUsername = "userRequester-" + suffix;
        createUser(requesterUsername, "user-requester-" + suffix + "@gmail.com");
        flushAndClear();
        MockHttpSession requesterSession = loginSession(requesterUsername);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "forbidden-update-" + suffix,
                "forbiddenPassword",
                "forbidden-update-" + suffix + "@gmail.com"
        );

        mockMvc.perform(multipart("/api/users/{userId}", targetUserId)
                        .file(jsonPart("userUpdateRequest", updateRequest))
                        .session(requesterSession)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        mockMvc.perform(delete("/api/users/{userId}", targetUserId)
                        .session(requesterSession)
                        .with(csrf()))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        flushAndClear();
        User targetUser = userRepository.findById(targetUserId).orElseThrow(AssertionError::new);
        assertThat(targetUser.getUsername()).isEqualTo(targetUsername);
        assertThat(targetUser.getEmail()).isEqualTo(targetEmail);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("채널 수정, 삭제 통합 성공 - 실제 DB에 변경사항 반영")
    void channelUpdateAndDelete_flowPersistsUpdatesAndRemovesChannel() throws Exception {
        // given
        // 채널 생성은 기존 통합 테스트에서 검증하므로, 여기서는 PUBLIC 채널을 만든 뒤 수정과 삭제 흐름을 검증한다.
        String suffix = uniqueSuffix();
        UUID channelId = createPublicChannel(
                "channel-update-" + suffix,
                "channel update description"
        );

        // when
        // PUBLIC 채널은 수정 가능하므로 이름과 설명을 변경한다.
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                "updated-channel-" + suffix,
                "updated channel description"
        );
        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
                .andExpect(jsonPath("$.name").value(updateRequest.newName()))
                .andExpect(jsonPath("$.description").value(updateRequest.newDescription()));

        flushAndClear();
        Channel updatedChannel = channelRepository.findById(channelId).orElseThrow(AssertionError::new);
        assertThat(updatedChannel.getName()).isEqualTo(updateRequest.newName());
        assertThat(updatedChannel.getDescription()).isEqualTo(updateRequest.newDescription());

        // when
        // 수정한 채널을 삭제한다.
        mockMvc.perform(delete("/api/channels/{channelId}", channelId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // then
        // Channel row가 실제 DB에서 제거되어야 한다.
        flushAndClear();
        assertThat(channelRepository.findById(channelId)).isEmpty();
    }

    @Test
    @DisplayName("메시지 수정, 삭제 통합 성공 - 실제 DB에 변경사항 반영")
    void messageUpdateAndDelete_flowPersistsUpdatesAndRemovesMessage() throws Exception {
        // given
        // 메시지 생성과 목록 조회는 기존 통합 테스트에서 검증한다.
        // 여기서는 생성된 메시지를 수정하고 삭제하는 API 흐름을 실제 DB와 함께 확인한다.
        String suffix = uniqueSuffix();
        String authorUsername = "messageUpdateAuthor-" + suffix;
        UUID authorId = createUser(authorUsername, "message-update-author-" + suffix + "@gmail.com");
        Channel channel = channelRepository.saveAndFlush(new Channel(new ChannelCreatePublicCommand(
                "message-update-channel-" + suffix,
                "message update channel description",
                ChannelType.PUBLIC
        )));
        flushAndClear();
        MockHttpSession authorSession = loginSession(authorUsername);
        UUID messageId = createMessage("message before update", channel.getId(), authorId, authorSession);

        // when
        // 메시지 내용을 수정한다.
        MessageUpdateRequest updateRequest = new MessageUpdateRequest("message after update");
        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .session(authorSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value(updateRequest.newContent()))
                .andExpect(jsonPath("$.channelId").value(channel.getId().toString()))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()));

        flushAndClear();
        Message updatedMessage = messageRepository.findById(messageId).orElseThrow(AssertionError::new);
        assertThat(updatedMessage.getContent()).isEqualTo(updateRequest.newContent());

        // when
        // 수정한 메시지를 삭제한다.
        mockMvc.perform(delete("/api/messages/{messageId}", messageId)
                        .session(authorSession)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // then
        // Message row가 실제 DB에서 제거되고, 메시지 파일 연결도 남아 있지 않아야 한다.
        flushAndClear();
        assertThat(messageRepository.findById(messageId)).isEmpty();
        assertThat(messageFileRepository.findAllByMessage_Id(messageId)).isEmpty();
    }

    @Test
    @DisplayName("메시지 수정, 삭제 인가 실패 - 작성자가 아니면 메시지를 변경할 수 없다")
    void messageUpdateAndDelete_returnsForbidden_whenRequesterIsNotAuthor() throws Exception {
        String suffix = uniqueSuffix();
        String authorUsername = "messageAuthor-" + suffix;
        UUID authorId = createUser(authorUsername, "message-author-" + suffix + "@gmail.com");
        String requesterUsername = "messageRequester-" + suffix;
        createUser(requesterUsername, "message-requester-" + suffix + "@gmail.com");
        Channel channel = channelRepository.saveAndFlush(new Channel(new ChannelCreatePublicCommand(
                "message-authorization-channel-" + suffix,
                "message authorization channel description",
                ChannelType.PUBLIC
        )));
        flushAndClear();

        MockHttpSession authorSession = loginSession(authorUsername);
        UUID messageId = createMessage("protected message", channel.getId(), authorId, authorSession);
        MockHttpSession requesterSession = loginSession(requesterUsername);
        MessageUpdateRequest updateRequest = new MessageUpdateRequest("forbidden message update");

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .session(requesterSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        mockMvc.perform(delete("/api/messages/{messageId}", messageId)
                        .session(requesterSession)
                        .with(csrf()))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        flushAndClear();
        Message message = messageRepository.findById(messageId).orElseThrow(AssertionError::new);
        assertThat(message.getContent()).isEqualTo("protected message");
        assertThat(message.getAuthor().getId()).isEqualTo(authorId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("사용자 역할 변경 통합 성공 - 응답과 DB에 변경된 역할이 반영")
    void updateUserRole_updatesResponseAndDatabase() throws Exception {
        String suffix = uniqueSuffix();
        UUID userId = createUser(
                "roleUser-" + suffix,
                "role-user-" + suffix + "@gmail.com"
        );
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(userId, Role.CHANNEL_MANAGER);

        mockMvc.perform(put("/api/auth/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.role").value(Role.CHANNEL_MANAGER.name()));

        flushAndClear();
        User updatedUser = userRepository.findById(userId).orElseThrow(AssertionError::new);
        assertThat(updatedUser.getRole()).isEqualTo(Role.CHANNEL_MANAGER);
    }

    @Test
    @DisplayName("애플리케이션 실행 시 관리자 계정이 한 번 초기화된다")
    void applicationStartup_initializesSingleAdminUser() {
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .toList();

        assertThat(adminUsers).hasSize(1);
        User adminUser = adminUsers.get(0);
        assertThat(adminUser.getUsername()).isEqualTo("test-admin");
        assertThat(adminUser.getEmail()).isEqualTo("test-admin@discodeit.local");
        assertThat(passwordEncoder.matches("test-admin-password", adminUser.getPassword())).isTrue();
    }

    private UUID createUser(String username, String email) throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                username,
                "integrationPassword",
                email
        );

        MvcResult result = mockMvc.perform(multipart("/api/users")
                        .file(jsonPart("userCreateRequest", request))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andReturn();

        return uuidAt(readBody(result), "/id");
    }

    private ResultActions performLogin(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .param("username", username)
                .param("password", password));
    }

    private MockHttpSession loginSession(String username) throws Exception {
        MvcResult loginResult = performLogin(username, "integrationPassword")
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(username))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private ResultActions performRememberMeLogin(String username, String password) throws Exception {
        return performLoginWithRememberMe(username, password, true);
    }

    private ResultActions performLoginWithRememberMe(
            String username,
            String password,
            boolean rememberMe
    ) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .param("username", username)
                .param("password", password)
                .param("remember-me", Boolean.toString(rememberMe)));
    }

    private UUID createPublicChannel(String name, String description) throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, description);

        MvcResult result = mockMvc.perform(post("/api/channels/public")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
                .andReturn();

        return uuidAt(readBody(result), "/id");
    }

    private UUID createMessage(String content, UUID channelId, UUID authorId) throws Exception {
        return createMessage(content, channelId, authorId, null);
    }

    private UUID createMessage(
            String content,
            UUID channelId,
            UUID authorId,
            MockHttpSession session
    ) throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(content, channelId, authorId);
        var requestBuilder = multipart("/api/messages")
                .file(jsonPart("messageCreateRequest", request))
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON);
        if (session != null) {
            requestBuilder.session(session);
        }

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        return uuidAt(readBody(result), "/id");
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name,
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }

    private MockMultipartFile filePart(String name, String fileName, String contentType, String content) {
        return new MockMultipartFile(
                name,
                fileName,
                contentType,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private UUID uuidAt(JsonNode node, String pointer) {
        return UUID.fromString(node.at(pointer).asText());
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
