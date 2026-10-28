package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.security.JwtRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.support.AsyncTestSupport.awaitIdle;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 실제 API·Spring 서비스·DB·로컬 파일 저장소를 연결한 업로드 및 삭제 회귀 테스트다.
 * 실제 커밋 뒤 업로드 결과를 기다리고 파일 내용과 관련 DB 행을 재조회한다.
 * 채널 삭제 성공 응답뿐 아니라 첨부 연결·메타데이터·실제 파일의 제거까지 확인한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:binary-upload-api;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "discodeit.storage.type=local"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("커밋 후 실제 파일 업로드·다운로드·삭제 API 통합 테스트")
class BinaryContentUploadApiIntegrationTest {
    @TempDir static Path uploadRoot;

    @DynamicPropertySource
    static void configureStorage(DynamicPropertyRegistry registry) {
        registry.add("discodeit.storage.local.root-path", () -> uploadRoot.toString());
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtRegistry jwtRegistry;
    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired ChannelRepository channelRepository;
    @Autowired MessageRepository messageRepository;
    @Autowired MessageFileRepository messageFileRepository;
    @Autowired BinaryContentRepository binaryContentRepository;
    @Autowired ReadStatusRepository readStatusRepository;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ThreadPoolTaskExecutor fileUploadExecutor;
    @Autowired ThreadPoolTaskExecutor notificationExecutor;

    private TransactionTemplate transaction;
    private final List<UUID> createdUserIds = new ArrayList<>();
    private final List<UUID> createdChannelIds = new ArrayList<>();
    private final List<UUID> createdMessageIds = new ArrayList<>();
    private final List<UUID> createdBinaryContentIds = new ArrayList<>();

    @BeforeEach
    void setUpTransaction() {
        transaction = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    void cleanCommittedFixtures() throws Exception {
        awaitIdle(fileUploadExecutor, notificationExecutor);
        try {
            transaction.executeWithoutResult(status -> {
                createdMessageIds.forEach(messageFileRepository::deleteByMessage_Id);
                messageRepository.deleteAllById(createdMessageIds);
                createdChannelIds.forEach(readStatusRepository::deleteByChannel_Id);
                createdUserIds.forEach(readStatusRepository::deleteByUser_Id);
                channelRepository.deleteAllById(createdChannelIds);
                createdUserIds.forEach(id -> notificationRepository.deleteAll(notificationRepository.findAllByReceiverId(id)));
                userRepository.deleteAllById(createdUserIds);
                binaryContentRepository.deleteAllById(createdBinaryContentIds);
            });
        } finally {
            createdUserIds.forEach(jwtRegistry::invalidateJwtInformationByUserId);
            for (UUID id : createdBinaryContentIds) {
                Files.deleteIfExists(uploadRoot.resolve(id.toString()));
            }
        }
    }

    @Test
    @DisplayName("사용자 생성 커밋 후 프로필이 완료 상태로 저장되고 다운로드된다")
    void createUser_uploadsProfileAfterCommit() throws Exception {
        String suffix = uniqueSuffix();
        String username = "profile-" + suffix;
        MockMultipartFile profile = filePart("profile", "avatar.png", "image/png", "profile-content");

        JsonNode body = createUserResponse(username, "profile-" + suffix + "@example.com", profile);

        UUID profileId = uuidAt(body, "/profile/id");
        UUID userId = uuidAt(body, "/id");
        assertThat(userRepository.findById(userId).orElseThrow().getProfileId()).isEqualTo(profileId);
        assertUploadedAndDownloadable(profileId, profile, loginAccessToken(username, "integrationPassword"));
    }

    @Test
    @DisplayName("프로필 변경 커밋 후 새 파일을 다운로드하고 기존 파일을 정리한다")
    void updateUser_replacesProfileAfterCommit() throws Exception {
        String suffix = uniqueSuffix();
        String username = "profile-" + suffix;
        MockMultipartFile oldProfile = filePart("profile", "old.png", "image/png", "old-profile");
        JsonNode created = createUserResponse(username, "profile-" + suffix + "@example.com", oldProfile);
        UUID userId = uuidAt(created, "/id");
        UUID oldProfileId = uuidAt(created, "/profile/id");
        String token = loginAccessToken(username, "integrationPassword");
        assertUploadedAndDownloadable(oldProfileId, oldProfile, token);
        MockMultipartFile newProfile = filePart("profile", "new.png", "image/png", "new-profile");

        MvcResult updated = mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId)
                        .file(jsonPart("userUpdateRequest", new UserUpdateRequest(null, null, null)))
                        .file(newProfile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        UUID newProfileId = uuidAt(readBody(updated), "/profile/id");
        createdBinaryContentIds.add(newProfileId);
        assertThat(newProfileId).isNotEqualTo(oldProfileId);
        assertThat(userRepository.findById(userId).orElseThrow().getProfileId()).isEqualTo(newProfileId);
        assertUploadedAndDownloadable(newProfileId, newProfile, token);
        assertThat(binaryContentRepository.findById(oldProfileId)).isEmpty();
        assertThat(uploadRoot.resolve(oldProfileId.toString())).doesNotExist();
    }

    @Test
    @DisplayName("공개 채널의 첨부 메시지를 저장·조회·다운로드하고 채널 삭제 시 관련 데이터와 파일을 제거한다")
    void publicChannelMessageAndAttachment_flowPersistsDownloadsAndDeletesChannel() throws Exception {
        // given
        // 실제 사용자와 공개 채널을 API로 만든 뒤, 그 사용자와 채널을 참조하는 메시지를 첨부 파일과 함께 생성한다.
        // 이 흐름은 Controller, Service, Repository, Mapper, Querydsl 목록 조회, 로컬 파일 스토리지를 함께 검증한다.
        String suffix = uniqueSuffix();
        String authorUsername = "messageAuthor-" + suffix;
        UUID authorId = createUser(authorUsername, "message-author-" + suffix + "@gmail.com");
        String authorAccessToken = loginAccessToken(authorUsername, "integrationPassword");
        String managerUsername = "channelManager-" + suffix;
        UUID managerId = createUser(managerUsername, "manager-" + suffix + "@gmail.com");
        User manager = userRepository.findById(managerId).orElseThrow();
        manager.updateRole(new UserRoleUpdateCommand(Role.CHANNEL_MANAGER));
        userRepository.saveAndFlush(manager);
        String managerAccessToken = loginAccessToken(managerUsername, "integrationPassword");

        PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest(
                "public-channel-" + suffix,
                "integration public channel"
        );
        MvcResult channelCreateResult = mockMvc.perform(post("/api/channels/public")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerAccessToken)
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
        createdChannelIds.add(channelId);

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
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorAccessToken)
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
        createdMessageIds.add(messageId);
        createdBinaryContentIds.add(attachmentId);
        awaitUpload(attachmentId);

        // 실제 DB에 Message, MessageFile, BinaryContent가 저장됐는지 확인한다.
        transaction.executeWithoutResult(status -> {
            Channel savedChannel = channelRepository.findById(channelId).orElseThrow(AssertionError::new);
            Message savedMessage = messageRepository.findById(messageId).orElseThrow(AssertionError::new);
            List<MessageFile> savedMessageFiles = messageFileRepository.findAllByMessage_Id(messageId);

            assertThat(savedChannel.getType()).isEqualTo(ChannelType.PUBLIC);
            assertThat(savedMessage.getContent()).isEqualTo(messageCreateRequest.content());
            assertThat(savedMessage.getChannelId()).isEqualTo(channelId);
            assertThat(savedMessage.getAuthor().getId()).isEqualTo(authorId);
            assertThat(savedMessageFiles).hasSize(1);
            assertThat(savedMessageFiles.get(0).getFileId()).isEqualTo(attachmentId);
            assertThat(binaryContentRepository.findById(attachmentId).orElseThrow().getStatus())
                    .isEqualTo(BinaryContentStatus.SUCCESS);
        });

        // when
        // 채널별 메시지 목록 API를 호출해 방금 만든 메시지가 Querydsl 목록 조회 경로로 조회되는지 확인한다.
        mockMvc.perform(get("/api/messages")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorAccessToken)
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
        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", attachmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorAccessToken))

                // then
                // BinaryContentService와 LocalBinaryContentStorage가 실제 저장 파일을 읽어 응답해야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().longValue("Content-Length", attachmentPart.getSize()))
                .andExpect(content().bytes(attachmentPart.getBytes()));

        // 업로드와 알림 후처리가 끝난 뒤 채널을 삭제한다.
        awaitIdle(fileUploadExecutor, notificationExecutor);
        mockMvc.perform(delete("/api/channels/{channelId}", channelId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerAccessToken)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 요청의 트랜잭션이 커밋된 뒤 별도 트랜잭션에서 삭제 결과를 확인한다.
        transaction.executeWithoutResult(status -> {
            assertThat(channelRepository.existsById(channelId)).isFalse();
            assertThat(messageRepository.existsById(messageId)).isFalse();
            assertThat(messageFileRepository.findAllByMessage_Id(messageId)).isEmpty();
            assertThat(binaryContentRepository.existsById(attachmentId)).isFalse();
        });

        // @AfterEach 정리 전에 커밋 후 실제 파일 삭제까지 완료됐는지 확인한다.
        assertThat(uploadRoot.resolve(attachmentId.toString())).doesNotExist();
    }

    private UUID createUser(String username, String email) throws Exception {
        return uuidAt(createUserResponse(username, email), "/id");
    }

    private JsonNode createUserResponse(String username, String email, MockMultipartFile... profiles) throws Exception {
        var request = multipart("/api/users")
                .file(jsonPart("userCreateRequest", new UserCreateRequest(username, "integrationPassword", email)));
        request.with(csrf());
        for (MockMultipartFile profile : profiles) {
            request.file(profile);
        }
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        JsonNode body = readBody(result);
        createdUserIds.add(uuidAt(body, "/id"));
        if (!body.path("profile").isNull() && body.path("profile").hasNonNull("id")) {
            createdBinaryContentIds.add(uuidAt(body, "/profile/id"));
        }
        return body;
    }

    private void assertUploadedAndDownloadable(UUID id, MockMultipartFile file, String token) throws Exception {
        awaitUpload(id);
        assertThat(binaryContentRepository.findById(id).orElseThrow().getStatus())
                .isEqualTo(BinaryContentStatus.SUCCESS);
        assertThat(Files.readAllBytes(uploadRoot.resolve(id.toString()))).containsExactly(file.getBytes());
        mockMvc.perform(get("/api/binaryContents/{id}/download", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(file.getContentType()))
                .andExpect(header().longValue("Content-Length", file.getSize()))
                .andExpect(content().bytes(file.getBytes()));
    }

    private String loginAccessToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk()).andReturn();
        String token = readBody(result).path("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private void awaitUpload(UUID id) {
        await().untilAsserted(() ->
                assertThat(binaryContentRepository.findById(id).orElseThrow().getStatus()).isEqualTo(BinaryContentStatus.SUCCESS));
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(value));
    }

    private MockMultipartFile filePart(String name, String fileName, String contentType, String content) {
        return new MockMultipartFile(name, fileName, contentType, content.getBytes(StandardCharsets.UTF_8));
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
}
