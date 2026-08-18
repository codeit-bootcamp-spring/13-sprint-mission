package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("메시지 API 통합 테스트")
class MessageApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("메시지 생성")
    class Create {

        @Test
        @DisplayName("유효한 요청이면 메시지를 생성하고 201 Created를 반환한다")
        void success() throws Exception {
            // given
            String userId = createUser(
                    "messageUser",
                    "message-user@test.com"
            );

            String channelId = createPublicChannel(
                    "메시지 채널",
                    "메시지 통합 테스트 채널"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            UUID.fromString(userId),
                            UUID.fromString(channelId)
                    );

            // when & then
            MvcResult result = mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.content")
                            .value("테스트 메시지"))
                    .andExpect(jsonPath("$.authorId")
                            .value(userId))
                    .andExpect(jsonPath("$.channelId")
                            .value(channelId))
                    .andExpect(jsonPath("$.author.id")
                            .value(userId))
                    .andExpect(jsonPath("$.author.username")
                            .value("messageUser"))
                    .andExpect(jsonPath("$.attachments")
                            .isArray())
                    .andExpect(jsonPath("$.attachments")
                            .isEmpty())
                    .andExpect(jsonPath("$.files")
                            .isArray())
                    .andExpect(jsonPath("$.files")
                            .isEmpty())
                    .andExpect(jsonPath("$.attachmentIds")
                            .doesNotExist())
                    .andReturn();

            String messageId = extractId(result);

            assertThat(result.getResponse().getHeader("Location"))
                    .isEqualTo("/api/messages/" + messageId);

            /*
             * 실제 H2 DB에 저장되었는지 단건 조회로 검증한다.
             */
            mockMvc.perform(get(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(messageId))
                    .andExpect(jsonPath("$.content")
                            .value("테스트 메시지"))
                    .andExpect(jsonPath("$.authorId")
                            .value(userId))
                    .andExpect(jsonPath("$.channelId")
                            .value(channelId));
        }

        @Test
        @DisplayName("메시지 내용이 비어 있으면 400 Bad Request를 반환한다")
        void blankContent() throws Exception {
            // given
            String userId = createUser(
                    "blankUser",
                    "blank-user@test.com"
            );

            String channelId = createPublicChannel(
                    "빈 메시지 테스트",
                    "빈 메시지 검증 채널"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "",
                            UUID.fromString(userId),
                            UUID.fromString(channelId)
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("작성자 ID가 없으면 400 Bad Request를 반환한다")
        void missingAuthorId() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "작성자 검증 채널",
                    "작성자 ID 검증"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "작성자 없는 메시지",
                            null,
                            UUID.fromString(channelId)
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("채널 ID가 없으면 400 Bad Request를 반환한다")
        void missingChannelId() throws Exception {
            // given
            String userId = createUser(
                    "missingChannelUser",
                    "missing-channel@test.com"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "채널 없는 메시지",
                            UUID.fromString(userId),
                            null
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 작성자로 메시지를 생성하면 404 Not Found를 반환한다")
        void authorNotFound() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "존재하지 않는 작성자 테스트",
                    "작성자 조회 실패 검증"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            UUID.randomUUID(),
                            UUID.fromString(channelId)
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 채널에 메시지를 생성하면 404 Not Found를 반환한다")
        void channelNotFound() throws Exception {
            // given
            String userId = createUser(
                    "channelNotFoundUser",
                    "channel-not-found@test.com"
            );

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            UUID.fromString(userId),
                            UUID.randomUUID()
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("메시지 목록 조회")
    class FindAllByChannelId {

        @Test
        @DisplayName("채널에 생성한 메시지를 최신순으로 조회한다")
        void success() throws Exception {
            // given
            String userId = createUser(
                    "listUser",
                    "list-user@test.com"
            );

            String channelId = createPublicChannel(
                    "목록 조회 채널",
                    "메시지 목록 조회 테스트"
            );

            String firstMessageId = createMessage(
                    "첫 번째 메시지",
                    userId,
                    channelId
            );

            String secondMessageId = createMessage(
                    "두 번째 메시지",
                    userId,
                    channelId
            );

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId)
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content.length()")
                            .value(2))
                    .andExpect(jsonPath("$.content[0].id")
                            .value(secondMessageId))
                    .andExpect(jsonPath("$.content[0].content")
                            .value("두 번째 메시지"))
                    .andExpect(jsonPath("$.content[1].id")
                            .value(firstMessageId))
                    .andExpect(jsonPath("$.content[1].content")
                            .value("첫 번째 메시지"))
                    .andExpect(jsonPath("$.size")
                            .value(10))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false));
        }

        @Test
        @DisplayName("메시지가 없으면 빈 목록을 반환한다")
        void empty() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "빈 목록 채널",
                    "메시지가 없는 채널"
            );

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content")
                            .isEmpty())
                    .andExpect(jsonPath("$.size")
                            .value(50))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false));
        }

        @Test
        @DisplayName("size보다 메시지가 많으면 hasNext와 nextCursor를 반환한다")
        void cursorFirstPage() throws Exception {
            // given
            String userId = createUser(
                    "cursorUser",
                    "cursor-user@test.com"
            );

            String channelId = createPublicChannel(
                    "커서 채널",
                    "커서 페이지네이션 테스트"
            );

            createMessage("메시지 1", userId, channelId);
            waitForDifferentCreatedAt();

            createMessage("메시지 2", userId, channelId);
            waitForDifferentCreatedAt();

            createMessage("메시지 3", userId, channelId);

            // when & then
            MvcResult result = mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId)
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()")
                            .value(2))
                    .andExpect(jsonPath("$.size")
                            .value(2))
                    .andExpect(jsonPath("$.hasNext")
                            .value(true))
                    .andExpect(jsonPath("$.nextCursor")
                            .isNotEmpty())
                    .andReturn();

            JsonNode body = objectMapper.readTree(
                    result.getResponse().getContentAsString()
            );

            String nextCursor =
                    body.get("nextCursor").asText();

            assertThat(Instant.parse(nextCursor)).isNotNull();
        }

        @Test
        @DisplayName("nextCursor를 이용해 다음 메시지 페이지를 조회한다")
        void should_ReturnNextMessagePage_when_NextCursorIsProvided()
                throws Exception {
            // given
            String userId = createUser(
                    "nextCursorUser",
                    "next-cursor@test.com"
            );

            String channelId = createPublicChannel(
                    "다음 커서 채널",
                    "다음 커서 페이지 조회"
            );

            createMessage(
                    "첫 번째 메시지",
                    userId,
                    channelId
            );

            waitForDifferentCreatedAt();

            createMessage(
                    "두 번째 메시지",
                    userId,
                    channelId
            );

            waitForDifferentCreatedAt();

            createMessage(
                    "세 번째 메시지",
                    userId,
                    channelId
            );

            MvcResult firstPage = mockMvc.perform(
                            get("/api/messages")
                                    .param("channelId", channelId)
                                    .param("size", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content.length()")
                            .value(2))
                    .andExpect(jsonPath("$.hasNext")
                            .value(true))
                    .andExpect(jsonPath("$.nextCursor")
                            .isNotEmpty())
                    .andReturn();

            JsonNode firstPageBody = objectMapper.readTree(
                    firstPage.getResponse().getContentAsString()
            );

            String nextCursor = firstPageBody
                    .get("nextCursor")
                    .asText();

            String firstPageFirstId = firstPageBody
                    .get("content")
                    .get(0)
                    .get("id")
                    .asText();

            String firstPageSecondId = firstPageBody
                    .get("content")
                    .get(1)
                    .get("id")
                    .asText();

            // when
            MvcResult nextPage = mockMvc.perform(
                            get("/api/messages")
                                    .param("channelId", channelId)
                                    .param("cursor", nextCursor)
                                    .param("size", "2")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content.length()")
                            .value(1))
                    .andExpect(jsonPath("$.size")
                            .value(2))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false))
                    .andReturn();

            JsonNode nextPageBody = objectMapper.readTree(
                    nextPage.getResponse().getContentAsString()
            );

            JsonNode nextPageContent =
                    nextPageBody.get("content");

            // then
            assertThat(nextPageContent).isNotNull();
            assertThat(nextPageContent.isArray()).isTrue();
            assertThat(nextPageContent.size()).isEqualTo(1);

            for (JsonNode message : nextPageContent) {
                String messageId =
                        message.get("id").asText();

                assertThat(messageId)
                        .isNotEqualTo(firstPageFirstId)
                        .isNotEqualTo(firstPageSecondId);
            }
        }

        @Test
        @DisplayName("after 파라미터를 cursor처럼 사용할 수 있다")
        void afterCompatibility() throws Exception {
            // given
            String userId = createUser(
                    "afterUser",
                    "after-user@test.com"
            );

            String channelId = createPublicChannel(
                    "after 호환 채널",
                    "after 커서 호환 테스트"
            );

            createMessage("첫 번째 메시지", userId, channelId);
            waitForDifferentCreatedAt();

            createMessage("두 번째 메시지", userId, channelId);
            waitForDifferentCreatedAt();

            createMessage("세 번째 메시지", userId, channelId);

            MvcResult firstPage = mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId)
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasNext")
                            .value(true))
                    .andExpect(jsonPath("$.nextCursor")
                            .isNotEmpty())
                    .andReturn();

            JsonNode firstPageBody = objectMapper.readTree(
                    firstPage.getResponse().getContentAsString()
            );

            String nextCursor = firstPageBody
                    .get("nextCursor")
                    .asText();

            MvcResult cursorResult = mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId)
                            .param("cursor", nextCursor)
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andReturn();

            MvcResult afterResult = mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId)
                            .param("after", nextCursor)
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode cursorBody = objectMapper.readTree(
                    cursorResult.getResponse().getContentAsString()
            );

            JsonNode afterBody = objectMapper.readTree(
                    afterResult.getResponse().getContentAsString()
            );

            assertThat(afterBody.get("content"))
                    .isEqualTo(cursorBody.get("content"));

            assertThat(afterBody.get("size"))
                    .isEqualTo(cursorBody.get("size"));

            assertThat(afterBody.get("hasNext"))
                    .isEqualTo(cursorBody.get("hasNext"));

            assertThat(afterBody.get("nextCursor"))
                    .isEqualTo(cursorBody.get("nextCursor"));
        }

        @Test
        @DisplayName("존재하지 않는 채널의 메시지 목록을 조회하면 404 Not Found를 반환한다")
        void channelNotFound() throws Exception {
            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    UUID.randomUUID().toString()
                            ))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class Update {

        @Test
        @DisplayName("생성한 메시지를 수정하면 변경된 내용이 반영된다")
        void success() throws Exception {
            // given
            String userId = createUser(
                    "updateMessageUser",
                    "update-message@test.com"
            );

            String channelId = createPublicChannel(
                    "메시지 수정 채널",
                    "메시지 수정 테스트"
            );

            String messageId = createMessage(
                    "수정 전 메시지",
                    userId,
                    channelId
            );

            String requestJson = """
                    {
                      "newContent": "수정 후 메시지"
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            messageId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(messageId))
                    .andExpect(jsonPath("$.content")
                            .value("수정 후 메시지"))
                    .andExpect(jsonPath("$.updatedAt")
                            .isNotEmpty());

            /*
             * 실제 H2 DB에 수정 내용이 반영됐는지 재조회한다.
             */
            mockMvc.perform(get(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .value("수정 후 메시지"));
        }

        @Test
        @DisplayName("수정할 내용이 없으면 400 Bad Request를 반환한다")
        void missingContent() throws Exception {
            // given
            String userId = createUser(
                    "missingUpdateUser",
                    "missing-update@test.com"
            );

            String channelId = createPublicChannel(
                    "수정 실패 채널",
                    "수정값 없음 검증"
            );

            String messageId = createMessage(
                    "기존 메시지",
                    userId,
                    channelId
            );

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            messageId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("수정할 내용이 공백이면 400 Bad Request를 반환한다")
        void blankContent() throws Exception {
            // given
            String userId = createUser(
                    "blankUpdateUser",
                    "blank-update@test.com"
            );

            String channelId = createPublicChannel(
                    "공백 수정 채널",
                    "공백 메시지 수정 검증"
            );

            String messageId = createMessage(
                    "기존 메시지",
                    userId,
                    channelId
            );

            String requestJson = """
                    {
                      "newContent": "   "
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            messageId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 수정하면 404 Not Found를 반환한다")
        void messageNotFound() throws Exception {
            // given
            String requestJson = """
                    {
                      "newContent": "수정된 메시지"
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            UUID.randomUUID()
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class Delete {

        @Test
        @DisplayName("생성한 메시지를 삭제하면 204 No Content를 반환한다")
        void success() throws Exception {
            // given
            String userId = createUser(
                    "deleteMessageUser",
                    "delete-message@test.com"
            );

            String channelId = createPublicChannel(
                    "메시지 삭제 채널",
                    "메시지 삭제 테스트"
            );

            String messageId = createMessage(
                    "삭제 대상 메시지",
                    userId,
                    channelId
            );

            // when
            mockMvc.perform(delete(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            // then
            mockMvc.perform(get(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/messages")
                            .param("channelId", channelId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 삭제하면 404 Not Found를 반환한다")
        void messageNotFound() throws Exception {
            // when & then
            mockMvc.perform(delete(
                            "/api/messages/{messageId}",
                            UUID.randomUUID()
                    ))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제하면 400 Bad Request를 반환한다")
        void invalidMessageId() throws Exception {
            // when & then
            mockMvc.perform(delete(
                            "/api/messages/{messageId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());
        }
    }

    private String createUser(
            String username,
            String email
    ) throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                username,
                email,
                "password",
                null
        );

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private String createPublicChannel(
            String name,
            String description
    ) throws Exception {
        ChannelCreateRequest request = new ChannelCreateRequest(
                null,
                name,
                description
        );

        MvcResult result = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private String createMessage(
            String content,
            String userId,
            String channelId
    ) throws Exception {
        MessageCreateRequest request =
                new MessageCreateRequest(
                        content,
                        UUID.fromString(userId),
                        UUID.fromString(channelId)
                );

        MvcResult result = mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private String extractId(MvcResult result) throws Exception {
        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();
    }

    private void waitForDifferentCreatedAt()
            throws InterruptedException {
        Thread.sleep(1000);
    }
}