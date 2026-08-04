package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.mapper.MessageMultipartMapper;
import com.sprint.mission.discodeit.service.MessageService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
@DisplayName("MessageController 슬라이스 테스트")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Controller 로직만 검증하기 위해 실제 MessageService 대신
     * Mockito Mock Bean을 사용한다.
     */
    @MockitoBean
    private MessageService messageService;

    /*
     * MessageController 생성자에 필요한 의존성이다.
     * 이번 테스트는 JSON API를 중심으로 하므로 실제 변환 로직은 실행하지 않는다.
     */
    @MockitoBean
    private MessageMultipartMapper messageMultipartMapper;

    /*
     * 메인 애플리케이션의 JPA Auditing 설정과
     * @WebMvcTest 슬라이스 사이의 충돌을 방지한다.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("메시지 생성")
    class Create {

        @Test
        @DisplayName("유효한 JSON 요청이면 메시지를 생성하고 201 Created를 반환한다")
        void success() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            authorId,
                            channelId,
                            null
                    );

            MessageResponse response =
                    new MessageResponse(
                            messageId,
                            createdAt,
                            null,
                            "테스트 메시지",
                            authorId,
                            channelId,
                            List.of()
                    );

            given(messageService.create(
                    any(MessageCreateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            "Location",
                            "/api/messages/" + messageId
                    ))
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.createdAt")
                            .value(createdAt.toString()))
                    .andExpect(jsonPath("$.content")
                            .value("테스트 메시지"))
                    .andExpect(jsonPath("$.authorId")
                            .value(authorId.toString()))
                    .andExpect(jsonPath("$.channelId")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.attachments")
                            .isArray())
                    .andExpect(jsonPath("$.attachments")
                            .isEmpty())
                    .andExpect(jsonPath("$.files")
                            .isArray())
                    .andExpect(jsonPath("$.files")
                            .isEmpty())
                    /*
                     * attachmentIds에는 @JsonIgnore가 적용되어 있으므로
                     * JSON 응답에 포함되지 않는지 검증한다.
                     */
                    .andExpect(jsonPath("$.attachmentIds")
                            .doesNotExist());

            then(messageService)
                    .should()
                    .create(argThat(actualRequest ->
                            "테스트 메시지".equals(
                                    actualRequest.getContent()
                            )
                                    && authorId.equals(
                                    actualRequest.getAuthorId()
                            )
                                    && channelId.equals(
                                    actualRequest.getChannelId()
                            )
                    ));
        }

        @Test
        @DisplayName("메시지 내용이 비어 있으면 400 Bad Request를 반환한다")
        void blankContent() throws Exception {
            // given
            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "",
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            null
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .create(any(MessageCreateRequest.class));
        }

        @Test
        @DisplayName("작성자 ID가 없으면 400 Bad Request를 반환한다")
        void missingAuthorId() throws Exception {
            // given
            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            null,
                            UUID.randomUUID(),
                            null
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .create(any(MessageCreateRequest.class));
        }

        @Test
        @DisplayName("채널 ID가 없으면 400 Bad Request를 반환한다")
        void missingChannelId() throws Exception {
            // given
            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "테스트 메시지",
                            UUID.randomUUID(),
                            null,
                            null
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .create(any(MessageCreateRequest.class));
        }

        @Test
        @DisplayName("메시지 내용이 1000자를 초과하면 400 Bad Request를 반환한다")
        void contentTooLong() throws Exception {
            // given
            MessageCreateRequest request =
                    new MessageCreateRequest(
                            "a".repeat(1001),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            null
                    );

            // when & then
            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .create(any(MessageCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("메시지 단건 조회")
    class Read {

        @Test
        @DisplayName("존재하는 메시지를 조회하면 200 OK와 메시지 정보를 반환한다")
        void success() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            Instant updatedAt =
                    Instant.parse("2026-07-28T02:00:00Z");

            MessageResponse response =
                    new MessageResponse(
                            messageId,
                            createdAt,
                            updatedAt,
                            "조회된 메시지",
                            authorId,
                            channelId,
                            List.of()
                    );

            given(messageService.read(messageId))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.createdAt")
                            .value(createdAt.toString()))
                    .andExpect(jsonPath("$.updatedAt")
                            .value(updatedAt.toString()))
                    .andExpect(jsonPath("$.content")
                            .value("조회된 메시지"))
                    .andExpect(jsonPath("$.authorId")
                            .value(authorId.toString()))
                    .andExpect(jsonPath("$.channelId")
                            .value(channelId.toString()));

            then(messageService)
                    .should()
                    .read(messageId);
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 조회하면 400 Bad Request를 반환한다")
        void invalidMessageId() throws Exception {
            // when & then
            mockMvc.perform(get(
                            "/api/messages/{messageId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널별 메시지 목록 조회")
    class FindAllByChannelId {

        @Test
        @DisplayName("첫 조회에서 size를 생략하면 기본값 50을 서비스에 전달한다")
        void firstPageWithDefaultSize() throws Exception {
            // given
            UUID channelId = UUID.randomUUID();
            UUID messageId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();

            MessageResponse message =
                    new MessageResponse(
                            messageId,
                            Instant.parse(
                                    "2026-07-28T03:00:00Z"
                            ),
                            null,
                            "최신 메시지",
                            authorId,
                            channelId,
                            List.of()
                    );

            PageResponse<MessageResponse> response =
                    new PageResponse<>(
                            List.of(message),
                            null,
                            1,
                            false,
                            1L
                    );

            given(messageService.findAllByChannelId(
                    channelId,
                    null,
                    50
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    channelId.toString()
                            ))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content.length()")
                            .value(1))
                    .andExpect(jsonPath("$.content[0].id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.content[0].content")
                            .value("최신 메시지"))
                    .andExpect(jsonPath("$.size")
                            .value(1))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false))
                    .andExpect(jsonPath("$.totalElements")
                            .value(1));

            then(messageService)
                    .should()
                    .findAllByChannelId(
                            channelId,
                            null,
                            50
                    );
        }

        @Test
        @DisplayName("cursor와 size를 서비스에 전달한다")
        void cursorPage() throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            Instant cursor =
                    Instant.parse("2026-07-28T03:00:00Z");

            Instant nextCursor =
                    Instant.parse("2026-07-28T02:00:00Z");

            PageResponse<MessageResponse> response =
                    new PageResponse<>(
                            List.of(),
                            nextCursor,
                            20,
                            true,
                            null
                    );

            given(messageService.findAllByChannelId(
                    channelId,
                    cursor,
                    20
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    channelId.toString()
                            )
                            .param(
                                    "cursor",
                                    cursor.toString()
                            )
                            .param(
                                    "size",
                                    "20"
                            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content")
                            .isEmpty())
                    .andExpect(jsonPath("$.nextCursor")
                            .value(nextCursor.toString()))
                    .andExpect(jsonPath("$.size")
                            .value(20))
                    .andExpect(jsonPath("$.hasNext")
                            .value(true));

            then(messageService)
                    .should()
                    .findAllByChannelId(
                            channelId,
                            cursor,
                            20
                    );
        }

        @Test
        @DisplayName("cursor가 없고 after만 있으면 after를 커서로 전달한다")
        void useAfterAsCursor() throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            Instant after =
                    Instant.parse("2026-07-28T03:00:00Z");

            PageResponse<MessageResponse> response =
                    new PageResponse<>(
                            List.of(),
                            null,
                            10,
                            false,
                            null
                    );

            given(messageService.findAllByChannelId(
                    channelId,
                    after,
                    10
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    channelId.toString()
                            )
                            .param(
                                    "after",
                                    after.toString()
                            )
                            .param(
                                    "size",
                                    "10"
                            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.size")
                            .value(10))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false));

            then(messageService)
                    .should()
                    .findAllByChannelId(
                            channelId,
                            after,
                            10
                    );
        }

        @Test
        @DisplayName("cursor와 after가 모두 있으면 cursor를 우선 사용한다")
        void cursorHasPriorityOverAfter() throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            Instant cursor =
                    Instant.parse("2026-07-28T03:00:00Z");

            Instant after =
                    Instant.parse("2026-07-28T02:00:00Z");

            PageResponse<MessageResponse> response =
                    new PageResponse<>(
                            List.of(),
                            null,
                            15,
                            false,
                            null
                    );

            given(messageService.findAllByChannelId(
                    channelId,
                    cursor,
                    15
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    channelId.toString()
                            )
                            .param(
                                    "cursor",
                                    cursor.toString()
                            )
                            .param(
                                    "after",
                                    after.toString()
                            )
                            .param(
                                    "size",
                                    "15"
                            ))
                    .andExpect(status().isOk());

            then(messageService)
                    .should()
                    .findAllByChannelId(
                            channelId,
                            cursor,
                            15
                    );
        }

        @Test
        @DisplayName("channelId 형식이 잘못되면 400 Bad Request를 반환한다")
        void invalidChannelId() throws Exception {
            // when & then
            mockMvc.perform(get("/api/messages")
                            .param(
                                    "channelId",
                                    "invalid-uuid"
                            ))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("channelId가 없으면 400 Bad Request를 반환한다")
        void missingChannelId() throws Exception {
            // when & then
            mockMvc.perform(get("/api/messages"))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class Update {

        @Test
        @DisplayName("경로의 메시지 ID와 수정 내용을 서비스에 전달하고 200 OK를 반환한다")
        void success() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            Instant updatedAt =
                    Instant.parse("2026-07-28T02:00:00Z");

            MessageResponse response =
                    new MessageResponse(
                            messageId,
                            createdAt,
                            updatedAt,
                            "수정된 메시지",
                            authorId,
                            channelId,
                            List.of()
                    );

            given(messageService.update(
                    any(MessageUpdateRequest.class)
            )).willReturn(response);

            /*
             * DTO를 ObjectMapper로 직렬화하면
             * content와 newContent가 함께 출력될 수 있으므로
             * 실제 API 요청 형태를 명확하게 검증하기 위해 JSON 문자열을 사용한다.
             */
            String requestJson = """
                    {
                      "newContent": "수정된 메시지"
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
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(messageId.toString()))
                    .andExpect(jsonPath("$.content")
                            .value("수정된 메시지"))
                    .andExpect(jsonPath("$.updatedAt")
                            .value(updatedAt.toString()))
                    .andExpect(jsonPath("$.authorId")
                            .value(authorId.toString()))
                    .andExpect(jsonPath("$.channelId")
                            .value(channelId.toString()));

            /*
             * 요청 본문의 ID가 아니라 URL 경로의 messageId가
             * 서비스 요청 객체에 반영되는지 검증한다.
             */
            then(messageService)
                    .should()
                    .update(argThat(fixedRequest ->
                            messageId.equals(fixedRequest.getId())
                                    && "수정된 메시지".equals(
                                    fixedRequest.getContent()
                            )
                    ));
        }

        @Test
        @DisplayName("수정할 메시지 내용이 없으면 400 Bad Request를 반환한다")
        void missingContent() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();

            String requestJson = """
                    {}
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            messageId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .update(any(MessageUpdateRequest.class));
        }

        @Test
        @DisplayName("수정할 메시지 내용이 공백이면 400 Bad Request를 반환한다")
        void blankContent() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();

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

            then(messageService)
                    .should(never())
                    .update(any(MessageUpdateRequest.class));
        }

        @Test
        @DisplayName("수정할 메시지 내용이 1000자를 초과하면 400 Bad Request를 반환한다")
        void contentTooLong() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();

            String requestJson = """
                    {
                      "newContent": "%s"
                    }
                    """.formatted("a".repeat(1001));

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            messageId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .should(never())
                    .update(any(MessageUpdateRequest.class));
        }

        @Test
        @DisplayName("잘못된 메시지 UUID 형식이면 400 Bad Request를 반환한다")
        void invalidMessageId() throws Exception {
            // given
            String requestJson = """
                    {
                      "newContent": "수정된 메시지"
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/messages/{messageId}",
                            "invalid-uuid"
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(messageService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class Delete {

        @Test
        @DisplayName("메시지를 삭제하면 204 No Content를 반환한다")
        void success() throws Exception {
            // given
            UUID messageId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete(
                            "/api/messages/{messageId}",
                            messageId
                    ))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            then(messageService)
                    .should()
                    .delete(messageId);
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

            then(messageService)
                    .shouldHaveNoInteractions();
        }
    }
}