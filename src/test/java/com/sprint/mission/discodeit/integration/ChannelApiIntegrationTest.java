package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
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
@DisplayName("채널 API 통합 테스트")
class ChannelApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("PUBLIC 채널 생성")
    class CreatePublicChannel {

        @Test
        @DisplayName("유효한 요청이면 PUBLIC 채널을 생성하고 201 Created를 반환한다")
        void success() throws Exception {
            // given
            ChannelCreateRequest request = new ChannelCreateRequest(
                    null,
                    "일반 채널",
                    "일반 대화 채널"
            );

            // when & then
            MvcResult result = mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("PUBLIC"))
                    .andExpect(jsonPath("$.name").value("일반 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("일반 대화 채널"))
                    .andReturn();

            String channelId = extractId(result);

            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(channelId))
                    .andExpect(jsonPath("$.type").value("PUBLIC"))
                    .andExpect(jsonPath("$.name").value("일반 채널"));

            assertThat(result.getResponse().getHeader("Location"))
                    .isEqualTo("/api/channels/" + channelId);
        }

        @Test
        @DisplayName("채널 이름이 비어 있으면 400 Bad Request를 반환한다")
        void blankName() throws Exception {
            // given
            ChannelCreateRequest request = new ChannelCreateRequest(
                    null,
                    "",
                    "채널 설명"
            );

            // when & then
            mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("채널 이름이 100자를 초과하면 400 Bad Request를 반환한다")
        void nameTooLong() throws Exception {
            // given
            ChannelCreateRequest request = new ChannelCreateRequest(
                    null,
                    "a".repeat(101),
                    "채널 설명"
            );

            // when & then
            mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PRIVATE 채널 생성")
    class CreatePrivateChannel {

        @Test
        @DisplayName("존재하는 사용자들로 PRIVATE 채널을 생성한다")
        void success() throws Exception {
            // given
            String firstUserId = createUser(
                    "user1",
                    "user1@test.com"
            );

            String secondUserId = createUser(
                    "user2",
                    "user2@test.com"
            );

            String requestJson = createPrivateChannelRequestJson(
                    firstUserId,
                    secondUserId
            );

            // when & then
            MvcResult result = mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("PRIVATE"))
                    .andExpect(jsonPath("$.participantUserIds").isArray())
                    .andExpect(jsonPath("$.participantUserIds.length()")
                            .value(2))
                    .andExpect(jsonPath("$.participantUserIds")
                            .value(org.hamcrest.Matchers.containsInAnyOrder(
                                    firstUserId,
                                    secondUserId
                            )))
                    .andReturn();

            String channelId = extractId(result);

            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(channelId))
                    .andExpect(jsonPath("$.type").value("PRIVATE"))
                    .andExpect(jsonPath("$.participantUserIds.length()")
                            .value(2));
        }

        @Test
        @DisplayName("참여자 목록이 비어 있으면 400 Bad Request를 반환한다")
        void emptyParticipants() throws Exception {
            // given
            String requestJson = """
                    {
                      "participantIds": []
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 포함되면 404 Not Found를 반환한다")
        void participantNotFound() throws Exception {
            // given
            String existingUserId = createUser(
                    "user1",
                    "user1@test.com"
            );

            String unknownUserId = UUID.randomUUID().toString();

            String requestJson = createPrivateChannelRequestJson(
                    existingUserId,
                    unknownUserId
            );

            // when & then
            mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("채널 수정")
    class Update {

        @Test
        @DisplayName("생성한 PUBLIC 채널을 수정하면 변경 내용이 반영된다")
        void success() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "수정 전 채널",
                    "수정 전 설명"
            );

            String requestJson = """
                    {
                      "newName": "수정 후 채널",
                      "newDescription": "수정 후 설명"
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(channelId))
                    .andExpect(jsonPath("$.name")
                            .value("수정 후 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("수정 후 설명"))
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());

            /*
             * 실제 H2 데이터베이스에 수정 내용이 반영됐는지
             * 다시 조회해 검증한다.
             */
            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name")
                            .value("수정 후 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("수정 후 설명"));
        }

        @Test
        @DisplayName("수정할 이름과 설명이 모두 없으면 400 Bad Request를 반환한다")
        void noUpdateValue() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "기존 채널",
                    "기존 설명"
            );

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PRIVATE 채널을 수정하면 클라이언트 오류를 반환한다")
        void privateChannelCannotBeUpdated() throws Exception {
            // given
            String firstUserId = createUser(
                    "privateUser1",
                    "private-user1@test.com"
            );

            String secondUserId = createUser(
                    "privateUser2",
                    "private-user2@test.com"
            );

            String channelId = createPrivateChannel(
                    firstUserId,
                    secondUserId
            );

            String requestJson = """
                    {
                      "newName": "수정된 비공개 채널",
                      "newDescription": "수정된 설명"
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().is4xxClientError());

            /*
             * 수정 실패 후에도 기존 채널이 PRIVATE 상태로
             * 유지되는지 확인한다.
             */
            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("PRIVATE"));
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class Delete {

        @Test
        @DisplayName("생성한 채널을 삭제하면 204 No Content를 반환한다")
        void success() throws Exception {
            // given
            String channelId = createPublicChannel(
                    "삭제 대상 채널",
                    "삭제 대상 설명"
            );

            // when
            mockMvc.perform(delete(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            // then
            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 404 Not Found를 반환한다")
        void channelNotFound() throws Exception {
            // given
            UUID unknownChannelId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete(
                            "/api/channels/{channelId}",
                            unknownChannelId
                    ))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제하면 400 Bad Request를 반환한다")
        void invalidChannelId() throws Exception {
            // when & then
            mockMvc.perform(delete(
                            "/api/channels/{channelId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());
        }
    }

    /*
     * 사용자 생성 API를 호출하고 생성된 사용자 ID를 반환한다.
     */
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

    /*
     * PUBLIC 채널 생성 API를 호출하고 생성된 채널 ID를 반환한다.
     */
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

    /*
     * PRIVATE 채널 생성 API를 호출하고 생성된 채널 ID를 반환한다.
     *
     * PrivateChannelCreateRequest를 ObjectMapper로 직렬화하지 않고
     * API 명세에 있는 participantIds 필드만 직접 전송한다.
     */
    private String createPrivateChannel(
            String firstUserId,
            String secondUserId
    ) throws Exception {
        String requestJson = createPrivateChannelRequestJson(
                firstUserId,
                secondUserId
        );

        MvcResult result = mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    /*
     * PRIVATE 채널 생성 API의 정확한 요청 JSON을 만든다.
     */
    private String createPrivateChannelRequestJson(
            String firstUserId,
            String secondUserId
    ) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of(
                        "participantIds",
                        java.util.List.of(
                                firstUserId,
                                secondUserId
                        )
                )
        );
    }

    /*
     * API 응답 JSON에서 id 값을 추출한다.
     */
    private String extractId(MvcResult result) throws Exception {
        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();
    }
}