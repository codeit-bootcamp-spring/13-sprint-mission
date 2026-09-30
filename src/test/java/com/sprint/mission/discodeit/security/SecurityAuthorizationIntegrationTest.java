package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.request.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security 인증·인가 통합 테스트")
class SecurityAuthorizationIntegrationTest {

    private static final String PRIVATE_MESSAGE_CONTENT = "private channel message";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserMapper userMapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserRoleUpdater userRoleUpdater;

    @Test
    @DisplayName("보호된 API에 미인증으로 접근하면 401을 반환한다")
    void protectedApi_returnsUnauthorized_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.status").value(401),
                        jsonPath("$.code").value("AUTH_401")
                );
    }

    @Test
    @DisplayName("Swagger, OpenAPI 문서와 정적 리소스는 인증 없이 접근할 수 있다")
    void nonApiResources_arePublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api/docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator는 미인증 사용자에게 401을 반환한다")
    void actuator_returnsUnauthorized_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Actuator는 일반 사용자에게 403을 반환한다")
    void actuator_returnsForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actuator는 관리자에게 접근을 허용한다")
    void actuator_returnsOk_whenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 사용자는 공개 채널을 생성할 수 없다")
    void createPublicChannel_returnsForbidden_whenUserHasUserRole() throws Exception {
        performPublicChannelCreate(saveUser(Role.USER))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.code").value("AUTH_403")
                );
    }

    @Test
    @DisplayName("채널 매니저는 공개 채널을 생성할 수 있다")
    void createPublicChannel_returnsCreated_whenUserIsChannelManager() throws Exception {
        performPublicChannelCreate(saveUser(Role.CHANNEL_MANAGER))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("관리자는 권한 계층을 통해 공개 채널을 생성할 수 있다")
    void createPublicChannel_returnsCreated_whenUserIsAdmin() throws Exception {
        performPublicChannelCreate(saveUser(Role.ADMIN))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자는 공개 채널을 수정하거나 삭제할 수 없다")
    void managePublicChannel_returnsForbidden_whenUserHasUserRole() throws Exception {
        Channel channel = savePublicChannel();
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                "updated-security-channel",
                "updated security authorization test"
        );

        mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId())
                        .with(csrf()))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        assertThat(channelRepository.existsById(channel.getId())).isTrue();
    }

    @Test
    @WithMockUser(roles = "CHANNEL_MANAGER")
    @DisplayName("채널 매니저는 공개 채널을 수정하고 삭제할 수 있다")
    void managePublicChannel_succeeds_whenUserIsChannelManager() throws Exception {
        Channel channel = savePublicChannel();
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                "updated-security-channel",
                "updated security authorization test"
        );

        mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(channelRepository.existsById(channel.getId())).isFalse();
    }

    @ParameterizedTest(name = "참여자 {0}명, 요청자 참여 여부 {1}")
    @CsvSource({"2, true", "2, false", "3, true", "3, false"})
    @DisplayName("일반 사용자는 참여 여부와 관계없이 DM과 그룹 DM을 삭제할 수 없다")
    void deletePrivateChannel_returnsForbidden_whenRequesterIsUser(
            int participantCount, boolean requesterIsParticipant
    ) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(participantCount);
        User requester = requesterIsParticipant
                ? userRepository.findById(fixture.participantIds().get(0)).orElseThrow()
                : saveUser(Role.USER);
        flushAndClear();
        assertThat(readStatusRepository.existsByChannel_IdAndUser_Id(fixture.channelId(), requester.getId()))
                .isEqualTo(requesterIsParticipant);

        mockMvc.perform(delete("/api/channels/{channelId}", fixture.channelId())
                        .with(user(userDetails(requester)))
                        .with(csrf()))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.code").value("AUTH_403")
                );

        assertPrivateChannelDataPreserved(fixture);
    }

    @ParameterizedTest(name = "참여자 {0}명, 요청자 권한 {1}")
    @CsvSource({"2, CHANNEL_MANAGER", "3, CHANNEL_MANAGER", "2, ADMIN", "3, ADMIN"})
    @DisplayName("채널 매니저와 관리자는 참여하지 않은 DM과 그룹 DM도 삭제할 수 있다")
    void deletePrivateChannel_succeeds_whenRequesterHasManagementRole(
            int participantCount, Role role
    ) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(participantCount);
        User requester = saveUser(role);
        flushAndClear();
        assertThat(readStatusRepository.existsByChannel_IdAndUser_Id(fixture.channelId(), requester.getId()))
                .isFalse();

        mockMvc.perform(delete("/api/channels/{channelId}", fixture.channelId())
                        .with(user(userDetails(requester)))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        flushAndClear();
        assertThat(channelRepository.existsById(fixture.channelId())).isFalse();
        assertThat(readStatusRepository.findByChannelId(fixture.channelId())).isEmpty();
        assertThat(messageRepository.existsByChannel_Id(fixture.channelId())).isFalse();
        assertThat(messageRepository.findById(fixture.messageId())).isEmpty();
    }

    @ParameterizedTest(name = "참여자 {0}명")
    @ValueSource(ints = {2, 3})
    @DisplayName("미인증 사용자는 DM과 그룹 DM을 삭제할 수 없다")
    void deletePrivateChannel_returnsUnauthorized_whenUnauthenticated(int participantCount) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(participantCount);

        mockMvc.perform(delete("/api/channels/{channelId}", fixture.channelId())
                        .with(csrf()))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.status").value(401),
                        jsonPath("$.code").value("AUTH_401")
                );

        assertPrivateChannelDataPreserved(fixture);
    }

    @ParameterizedTest(name = "권한 {0}, 생성자 참여 {1}")
    @CsvSource({"USER, true", "USER, false", "CHANNEL_MANAGER, true", "CHANNEL_MANAGER, false",
            "ADMIN, true", "ADMIN, false"})
    @DisplayName("관리 역할은 참여하지 않아도 비공개 채널을 생성하고 일반 사용자는 본인을 포함해야 한다")
    void createPrivateChannel_appliesRoleAndMembershipPolicy(Role role, boolean included) throws Exception {
        User requester = saveUser(role);
        User first = included ? requester : saveUser(Role.USER);
        User second = saveUser(Role.USER);
        List<UUID> participants = List.of(first.getId(), second.getId());
        long channelsBefore = channelRepository.count();
        long readStatusesBefore = readStatusRepository.count();

        var result = mockMvc.perform(post("/api/channels/private")
                .with(user(userDetails(requester))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PrivateChannelCreateRequest(participants))));

        if (role == Role.USER && !included) {
            result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            flushAndClear();
            assertThat(channelRepository.count()).isEqualTo(channelsBefore);
            assertThat(readStatusRepository.count()).isEqualTo(readStatusesBefore);
        } else {
            String body = result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            UUID channelId = UUID.fromString(objectMapper.readTree(body).path("id").asText());
            flushAndClear();
            assertThat(readStatusRepository.findByChannelId(channelId)).extracting(ReadStatus::getUserId)
                    .containsExactlyInAnyOrderElementsOf(participants);
            // 관리 역할은 참여하지 않아도 조회할 수 있지만 참여자로 등록되지는 않는다.
            mockMvc.perform(get("/api/messages").param("channelId", channelId.toString())
                            .with(user(userDetails(requester))))
                    .andExpect(status().isOk());
            flushAndClear();
            assertThat(readStatusRepository.findByChannelId(channelId)).extracting(ReadStatus::getUserId)
                    .containsExactlyInAnyOrderElementsOf(participants);
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("일반 사용자는 본인의 공개·참여 채널을 조회하고 관리 역할은 모든 채널을 조회한다")
    void channelLists_applyRequesterRoleAndIdentity(Role role) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(2);
        PrivateChannelFixture otherPrivate = savePrivateChannelWithData(3);
        Channel publicChannel = savePublicChannel();
        User requester = userRepository.findById(fixture.participantIds().get(0)).orElseThrow();
        requester.updateRole(new UserRoleUpdateCommand(role));
        flushAndClear();

        String[] expectedIds = role == Role.USER
                ? new String[]{publicChannel.getId().toString(), fixture.channelId().toString()}
                : new String[]{publicChannel.getId().toString(), fixture.channelId().toString(),
                        otherPrivate.channelId().toString()};
        for (UUID targetUserId : List.of(requester.getId(), otherPrivate.participantIds().get(0))) {
            var result = mockMvc.perform(get("/api/channels").param("userId", targetUserId.toString())
                    .with(user(userDetails(requester))));
            if (role == Role.USER && !requester.getId().equals(targetUserId)) {
                result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            } else {
                result.andExpect(status().isOk())
                        .andExpect(jsonPath("$[*].id", containsInAnyOrder(expectedIds)));
            }
        }
        assertPrivateChannelDataPreserved(otherPrivate);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("관리 역할도 읽음 상태 목록은 본인 것만 조회한다")
    void readStatusLists_requireRequesterIdentity(Role role) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(2);
        User requester = userRepository.findById(fixture.participantIds().get(0)).orElseThrow();
        requester.updateRole(new UserRoleUpdateCommand(role));
        flushAndClear();

        mockMvc.perform(get("/api/readStatuses").param("userId", requester.getId().toString())
                        .with(user(userDetails(requester))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(requester.getId().toString()));

        mockMvc.perform(get("/api/readStatuses").param("userId", fixture.participantIds().get(1).toString())
                        .with(user(userDetails(requester))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("조회 대상 채널과 읽음 상태가 없으면 모든 역할이 빈 목록을 조회한다")
    void userLists_allowEmptyResults(Role role) throws Exception {
        User requester = saveUser(role);
        for (String path : List.of("/api/channels", "/api/readStatuses")) {
            mockMvc.perform(get(path).param("userId", requester.getId().toString())
                            .with(user(userDetails(requester))))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("관리 역할은 비참여 채널도 조회하고 일반 사용자는 공개·참여 채널만 조회한다")
    void messageLists_applyRoleAndChannelAccess(Role role) throws Exception {
        PrivateChannelFixture accessible = savePrivateChannelWithData(2);
        PrivateChannelFixture inaccessible = savePrivateChannelWithData(3);
        User requester = userRepository.findById(accessible.participantIds().get(0)).orElseThrow();
        requester.updateRole(new UserRoleUpdateCommand(role));
        Channel publicChannel = savePublicChannel();
        flushAndClear();

        mockMvc.perform(get("/api/messages").param("channelId", accessible.channelId().toString())
                        .with(user(userDetails(requester))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(accessible.messageId().toString()));
        mockMvc.perform(get("/api/messages").param("channelId", publicChannel.getId().toString())
                        .with(user(userDetails(requester))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        var result = mockMvc.perform(get("/api/messages").param("channelId", inaccessible.channelId().toString())
                .with(user(userDetails(requester))));
        if (role == Role.USER) {
            result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
        } else {
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(inaccessible.messageId().toString()));
        }
        assertPrivateChannelDataPreserved(inaccessible);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("존재하지 않는 채널의 메시지 조회는 일반 사용자에게 거부되고 관리 역할에는 빈 목록을 반환한다")
    void messageLists_handleMissingChannelAccordingToRole(Role role) throws Exception {
        User requester = saveUser(role);
        var result = mockMvc.perform(get("/api/messages").param("channelId", UUID.randomUUID().toString())
                .with(user(userDetails(requester))));
        if (role == Role.USER) {
            result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
        } else {
            result.andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("읽음 상태 생성으로 다른 사람을 사칭하거나 비공개 채널에 임의로 참여할 수 없다")
    void readStatusCreation_preventsMembershipBypass(Role role) throws Exception {
        User requester = saveUser(role);
        User other = saveUser(Role.USER);
        Channel publicChannel = savePublicChannel();
        PrivateChannelFixture fixture = savePrivateChannelWithData(2);
        Instant readAt = Instant.parse("2026-09-25T00:00:00Z");

        mockMvc.perform(post("/api/readStatuses").with(user(userDetails(requester))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReadStatusCreateRequest(
                                requester.getId(), publicChannel.getId(), readAt))))
                .andExpect(status().isCreated());
        flushAndClear();
        assertThat(readStatusRepository.existsByChannel_IdAndUser_Id(publicChannel.getId(), requester.getId()))
                .isTrue();

        List<ReadStatusCreateRequest> denied = List.of(
                new ReadStatusCreateRequest(other.getId(), publicChannel.getId(), readAt),
                new ReadStatusCreateRequest(requester.getId(), fixture.channelId(), readAt),
                new ReadStatusCreateRequest(requester.getId(), UUID.randomUUID(), readAt));
        for (ReadStatusCreateRequest request : denied) {
            mockMvc.perform(post("/api/readStatuses").with(user(userDetails(requester))).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            flushAndClear();
            assertThat(readStatusRepository.existsByChannel_IdAndUser_Id(request.channelId(), request.userId()))
                    .isFalse();
        }
        assertPrivateChannelDataPreserved(fixture);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("메시지는 본인 명의로만 생성할 수 있고 사칭 요청은 저장되지 않는다")
    void messageCreation_requiresMatchingAuthor(Role role) throws Exception {
        User requester = saveUser(role);
        User other = saveUser(Role.USER);
        Channel channel = savePublicChannel();
        long before = messageRepository.count();
        for (boolean spoofed : List.of(true, false)) {
            var request = new MessageCreateRequest("identity test", channel.getId(),
                    spoofed ? other.getId() : requester.getId());
            var result = mockMvc.perform(multipart("/api/messages")
                    .file(new MockMultipartFile("messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                            objectMapper.writeValueAsBytes(request)))
                    .with(user(userDetails(requester))).with(csrf()));
            if (spoofed) {
                result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
                flushAndClear();
                assertThat(messageRepository.count()).isEqualTo(before);
            } else {
                String body = result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
                UUID messageId = UUID.fromString(objectMapper.readTree(body).path("id").asText());
                flushAndClear();
                assertThat(messageRepository.findById(messageId)).hasValueSatisfying(message ->
                        assertThat(message.getAuthor().getId()).isEqualTo(requester.getId()));
            }
        }
    }

    @ParameterizedTest(name = "권한 {0}, 참여 여부 {1}")
    @CsvSource({"USER, true", "USER, false", "CHANNEL_MANAGER, true", "CHANNEL_MANAGER, false",
            "ADMIN, true", "ADMIN, false"})
    @DisplayName("비공개 메시지 작성은 관리 역할 또는 참여자에게 허용하며 참여 상태는 변경하지 않는다")
    void privateMessageCreation_appliesRoleAndMembershipPolicy(Role role, boolean participating) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(3);
        User requester = participating
                ? userRepository.findById(fixture.participantIds().get(0)).orElseThrow()
                : saveUser(role);
        requester.updateRole(new UserRoleUpdateCommand(role));
        flushAndClear();
        long messagesBefore = messageRepository.count();
        var request = new MessageCreateRequest("private message", fixture.channelId(), requester.getId());
        var result = mockMvc.perform(multipart("/api/messages")
                .file(new MockMultipartFile("messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                        objectMapper.writeValueAsBytes(request)))
                .with(user(userDetails(requester))).with(csrf()));

        if (role == Role.USER && !participating) {
            result.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            flushAndClear();
            assertThat(messageRepository.count()).isEqualTo(messagesBefore);
        } else {
            String body = result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            UUID messageId = UUID.fromString(objectMapper.readTree(body).path("id").asText());
            flushAndClear();
            assertThat(messageRepository.count()).isEqualTo(messagesBefore + 1);
            assertThat(messageRepository.findById(messageId)).hasValueSatisfying(message -> {
                assertThat(message.getAuthor().getId()).isEqualTo(requester.getId());
                assertThat(message.getChannelId()).isEqualTo(fixture.channelId());
            });
        }
        assertPrivateChannelDataPreserved(fixture);
    }

    @ParameterizedTest(name = "권한 {0}, 요청자 {1}")
    @CsvSource({"USER, AUTHOR", "USER, PARTICIPANT", "USER, OUTSIDER",
            "CHANNEL_MANAGER, AUTHOR", "CHANNEL_MANAGER, PARTICIPANT", "CHANNEL_MANAGER, OUTSIDER",
            "ADMIN, AUTHOR", "ADMIN, PARTICIPANT", "ADMIN, OUTSIDER"})
    @DisplayName("메시지 수정·삭제는 모든 역할에서 작성자에게만 허용한다")
    void messageManagement_requiresOwnershipRegardlessOfRole(Role role, String requesterType) throws Exception {
        PrivateChannelFixture fixture = savePrivateChannelWithData(2);
        User requester = switch (requesterType) {
            case "AUTHOR" -> userRepository.findById(fixture.participantIds().get(0)).orElseThrow();
            case "PARTICIPANT" -> userRepository.findById(fixture.participantIds().get(1)).orElseThrow();
            case "OUTSIDER" -> saveUser(role);
            default -> throw new IllegalArgumentException(requesterType);
        };
        requester.updateRole(new UserRoleUpdateCommand(role));
        flushAndClear();
        boolean allowed = requesterType.equals("AUTHOR");

        var updateResult = mockMvc.perform(patch("/api/messages/{messageId}", fixture.messageId())
                .with(user(userDetails(requester))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new MessageUpdateRequest("managed content"))));
        if (allowed) {
            updateResult.andExpect(status().isOk()).andExpect(jsonPath("$.content").value("managed content"));
            flushAndClear();
            assertThat(messageRepository.findById(fixture.messageId())).hasValueSatisfying(message -> {
                assertThat(message.getContent()).isEqualTo("managed content");
                assertThat(message.getAuthor().getId()).isEqualTo(fixture.participantIds().get(0));
            });
        } else {
            updateResult.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            assertPrivateChannelDataPreserved(fixture);
        }

        var deleteResult = mockMvc.perform(delete("/api/messages/{messageId}", fixture.messageId())
                .with(user(userDetails(requester))).with(csrf()));
        if (allowed) {
            deleteResult.andExpect(status().isNoContent());
            flushAndClear();
            assertThat(messageRepository.existsById(fixture.messageId())).isFalse();
            assertThat(channelRepository.existsById(fixture.channelId())).isTrue();
            assertThat(readStatusRepository.findByChannelId(fixture.channelId())).extracting(ReadStatus::getUserId)
                    .containsExactlyInAnyOrderElementsOf(fixture.participantIds());
        } else {
            deleteResult.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("AUTH_403"));
            assertPrivateChannelDataPreserved(fixture);
        }
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자가 역할 변경 Service를 직접 호출하면 인가가 거부된다")
    void updateUserRoleService_isDenied_whenUserIsNotAdmin() {
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.CHANNEL_MANAGER);

        assertThatThrownBy(() -> userRoleUpdater.updateRole(UUID.randomUUID(), command))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("관리자와 채널 매니저의 권한 계층을 적용한다")
    void roleHierarchy_containsInheritedRoles() {
        List<String> adminAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                ).stream()
                .map(authority -> authority.getAuthority())
                .toList();
        List<String> channelManagerAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                        List.of(new SimpleGrantedAuthority("ROLE_CHANNEL_MANAGER"))
                ).stream()
                .map(authority -> authority.getAuthority())
                .toList();

        assertThat(adminAuthorities)
                .contains("ROLE_ADMIN", "ROLE_CHANNEL_MANAGER", "ROLE_USER");
        assertThat(channelManagerAuthorities)
                .contains("ROLE_CHANNEL_MANAGER", "ROLE_USER")
                .doesNotContain("ROLE_ADMIN");
    }

    private org.springframework.test.web.servlet.ResultActions performPublicChannelCreate(User requester) throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "security-public-channel",
                "security authorization test"
        );

        return mockMvc.perform(post("/api/channels/public")
                .with(user(userDetails(requester)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private Channel savePublicChannel() {
        return channelRepository.save(new Channel(new ChannelCreatePublicCommand(
                "security-public-channel",
                "security authorization test",
                ChannelType.PUBLIC
        )));
    }

    private User saveUser(Role role) {
        String suffix = UUID.randomUUID().toString();
        User user = new User(new UserCreateCommand(
                "user-" + suffix,
                "unused-password",
                suffix + "@security.test"
        ), null);
        user.updateRole(new UserRoleUpdateCommand(role));
        return userRepository.save(user);
    }

    private DiscodeitUserDetails userDetails(User user) {
        return new DiscodeitUserDetails(userMapper.toDto(user), user.getPassword());
    }

    private PrivateChannelFixture savePrivateChannelWithData(int participantCount) {
        List<User> participants = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            participants.add(saveUser(Role.USER));
        }
        List<UUID> participantIds = participants.stream().map(User::getId).toList();
        Channel channel = channelRepository.save(new Channel(new ChannelCreatePrivateCommand(
                participantIds, ChannelType.PRIVATE
        )));
        Instant readAt = Instant.parse("2026-09-25T00:00:00Z");
        List<UUID> readStatusIds = participants.stream()
                .map(participant -> readStatusRepository.save(new ReadStatus(
                        channel, participant, new ReadStatusCreateCommand(participant.getId(), readAt)
                )).getId())
                .toList();
        User author = participants.get(0);
        Message message = messageRepository.save(new Message(author, channel, new MessageCreateCommand(
                PRIVATE_MESSAGE_CONTENT, author.getId(), channel.getId()
        )));
        flushAndClear();

        return new PrivateChannelFixture(channel.getId(), participantIds, readStatusIds, message.getId(), readAt);
    }

    private void assertPrivateChannelDataPreserved(PrivateChannelFixture fixture) {
        flushAndClear();
        assertThat(channelRepository.findById(fixture.channelId()))
                .hasValueSatisfying(channel -> assertThat(channel.isPrivate()).isTrue());
        List<ReadStatus> readStatuses = readStatusRepository.findByChannelId(fixture.channelId());
        assertThat(readStatuses).extracting(ReadStatus::getId)
                .containsExactlyInAnyOrderElementsOf(fixture.readStatusIds());
        assertThat(readStatuses).extracting(ReadStatus::getUserId)
                .containsExactlyInAnyOrderElementsOf(fixture.participantIds());
        assertThat(readStatuses).allSatisfy(readStatus ->
                assertThat(readStatus.getLastReadAt()).isEqualTo(fixture.readAt()));
        assertThat(messageRepository.findById(fixture.messageId())).hasValueSatisfying(message -> {
            assertThat(message.getContent()).isEqualTo(PRIVATE_MESSAGE_CONTENT);
            assertThat(message.getChannelId()).isEqualTo(fixture.channelId());
            assertThat(message.getAuthor().getId()).isEqualTo(fixture.participantIds().get(0));
        });
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record PrivateChannelFixture(
            UUID channelId,
            List<UUID> participantIds,
            List<UUID> readStatusIds,
            UUID messageId,
            Instant readAt
    ) {
    }
}
