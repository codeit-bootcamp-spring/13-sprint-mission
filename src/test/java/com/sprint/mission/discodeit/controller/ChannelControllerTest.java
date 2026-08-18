package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
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

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
@DisplayName("ChannelController 슬라이스 테스트")
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Controller 테스트에서는 실제 서비스 로직을 실행하지 않는다.
     */
    @MockitoBean
    private ChannelService channelService;

    /*
     * 메인 애플리케이션의 @EnableJpaAuditing과
     * @WebMvcTest의 충돌을 방지한다.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("PUBLIC 채널 생성")
    class CreatePublicChannel {

        @Test
        @DisplayName("유효한 요청이면 PUBLIC 채널을 생성하고 201 Created를 반환한다")
        void should_ReturnCreatedPublicChannel_when_RequestIsValid()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();
            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            ChannelCreateRequest request =
                    new ChannelCreateRequest(
                            null,
                            "일반 채널",
                            "일반 대화 채널"
                    );

            ChannelResponse response =
                    new ChannelResponse(
                            channelId,
                            createdAt,
                            null,
                            ChannelType.PUBLIC,
                            "일반 채널",
                            "일반 대화 채널",
                            null,
                            List.of()
                    );

            given(channelService.createPublicChannel(
                    any(ChannelCreateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            "Location",
                            "/api/channels/" + channelId
                    ))
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.createdAt")
                            .value(createdAt.toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("일반 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("일반 대화 채널"))
                    .andExpect(jsonPath("$.participantUserIds")
                            .isArray())
                    .andExpect(jsonPath("$.participantIds")
                            .isArray())
                    .andExpect(jsonPath("$.participants")
                            .isArray());

            /*
             * Controller가 요청의 type을 PUBLIC으로 강제해서
             * 서비스에 전달하는지 검증한다.
             */
            then(channelService)
                    .should()
                    .createPublicChannel(argThat(fixedRequest ->
                            fixedRequest.getType()
                                    == ChannelType.PUBLIC
                                    && fixedRequest.getName()
                                    .equals("일반 채널")
                                    && fixedRequest.getDescription()
                                    .equals("일반 대화 채널")
                    ));
        }

        @Test
        @DisplayName("채널 이름이 비어 있으면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_PublicChannelNameIsBlank()
                throws Exception {
            // given
            ChannelCreateRequest request =
                    new ChannelCreateRequest(
                            null,
                            "",
                            "채널 설명"
                    );

            // when & then
            mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .createPublicChannel(
                            any(ChannelCreateRequest.class)
                    );
        }

        @Test
        @DisplayName("채널 이름이 100자를 초과하면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_PublicChannelNameExceedsMaxLength()
                throws Exception {
            // given
            ChannelCreateRequest request =
                    new ChannelCreateRequest(
                            null,
                            "a".repeat(101),
                            "채널 설명"
                    );

            // when & then
            mockMvc.perform(post("/api/channels/public")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .createPublicChannel(
                            any(ChannelCreateRequest.class)
                    );
        }
    }

    @Nested
    @DisplayName("PRIVATE 채널 생성")
    class CreatePrivateChannel {

        @Test
        @DisplayName("유효한 참여자 목록이면 PRIVATE 채널을 생성하고 201 Created를 반환한다")
        void should_ReturnCreatedPrivateChannel_when_ParticipantsAreValid()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();
            UUID firstUserId = UUID.randomUUID();
            UUID secondUserId = UUID.randomUUID();

            Instant createdAt =
                    Instant.parse("2026-07-28T01:00:00Z");

            PrivateChannelCreateRequest request =
                    new PrivateChannelCreateRequest(
                            List.of(firstUserId, secondUserId)
                    );

            ChannelResponse response =
                    new ChannelResponse(
                            channelId,
                            createdAt,
                            null,
                            ChannelType.PRIVATE,
                            null,
                            null,
                            null,
                            List.of(firstUserId, secondUserId)
                    );

            given(channelService.createPrivateChannel(
                    any(PrivateChannelCreateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            "Location",
                            "/api/channels/" + channelId
                    ))
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PRIVATE"))
                    .andExpect(jsonPath("$.participantUserIds.length()")
                            .value(2))
                    .andExpect(jsonPath("$.participantUserIds[0]")
                            .value(firstUserId.toString()))
                    .andExpect(jsonPath("$.participantUserIds[1]")
                            .value(secondUserId.toString()))
                    .andExpect(jsonPath("$.participantIds.length()")
                            .value(2));

            then(channelService)
                    .should()
                    .createPrivateChannel(
                            any(PrivateChannelCreateRequest.class)
                    );
        }

        @Test
        @DisplayName("참여자 목록이 비어 있으면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_ParticipantListIsEmpty()
                throws Exception {
            // given
            PrivateChannelCreateRequest request =
                    new PrivateChannelCreateRequest(List.of());

            // when & then
            mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .createPrivateChannel(
                            any(PrivateChannelCreateRequest.class)
                    );
        }

        @Test
        @DisplayName("참여자 ID에 null이 포함되면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_ParticipantIdContainsNull()
                throws Exception {
            // given
            String requestJson = """
                    {
                      "participantIds": [null]
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/channels/private")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .createPrivateChannel(
                            any(PrivateChannelCreateRequest.class)
                    );
        }
    }

    @Nested
    @DisplayName("채널 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하는 채널을 조회하면 200 OK와 채널 정보를 반환한다")
        void should_ReturnChannelResponse_when_ChannelExists()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            ChannelResponse response =
                    new ChannelResponse(
                            channelId,
                            Instant.parse(
                                    "2026-07-28T01:00:00Z"
                            ),
                            null,
                            ChannelType.PUBLIC,
                            "일반 채널",
                            "일반 대화 채널",
                            Instant.parse(
                                    "2026-07-28T02:00:00Z"
                            ),
                            List.of()
                    );

            given(channelService.find(channelId))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("일반 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("일반 대화 채널"))
                    .andExpect(jsonPath("$.lastMessageAt")
                            .value("2026-07-28T02:00:00Z"));

            then(channelService)
                    .should()
                    .find(channelId);
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 조회하면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_ChannelIdFormatIsInvalid()
                throws Exception {
            // when & then
            mockMvc.perform(get(
                            "/api/channels/{channelId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("사용자별 채널 목록 조회")
    class FindAllByUserId {

        @Test
        @DisplayName("사용자가 참여한 채널 목록을 JSON 배열로 반환한다")
        void should_ReturnChannelList_when_UserParticipatesInChannels()
                throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID publicChannelId = UUID.randomUUID();
            UUID privateChannelId = UUID.randomUUID();

            ChannelResponse publicChannel =
                    new ChannelResponse(
                            publicChannelId,
                            Instant.parse(
                                    "2026-07-28T01:00:00Z"
                            ),
                            null,
                            ChannelType.PUBLIC,
                            "일반 채널",
                            "일반 대화 채널",
                            null,
                            List.of()
                    );

            ChannelResponse privateChannel =
                    new ChannelResponse(
                            privateChannelId,
                            Instant.parse(
                                    "2026-07-28T02:00:00Z"
                            ),
                            null,
                            ChannelType.PRIVATE,
                            null,
                            null,
                            null,
                            List.of(userId)
                    );

            given(channelService.findAllByUserId(userId))
                    .willReturn(List.of(
                            publicChannel,
                            privateChannel
                    ));

            // when & then
            mockMvc.perform(get("/api/channels")
                            .param(
                                    "userId",
                                    userId.toString()
                            ))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()")
                            .value(2))
                    .andExpect(jsonPath("$[0].id")
                            .value(publicChannelId.toString()))
                    .andExpect(jsonPath("$[0].type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$[1].id")
                            .value(privateChannelId.toString()))
                    .andExpect(jsonPath("$[1].type")
                            .value("PRIVATE"));

            then(channelService)
                    .should()
                    .findAllByUserId(userId);
        }

        @Test
        @DisplayName("채널이 없으면 빈 JSON 배열을 반환한다")
        void should_ReturnEmptyArray_when_UserHasNoChannels()
                throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            given(channelService.findAllByUserId(userId))
                    .willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/channels")
                            .param(
                                    "userId",
                                    userId.toString()
                            ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            then(channelService)
                    .should()
                    .findAllByUserId(userId);
        }

        @Test
        @DisplayName("userId 형식이 잘못되면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_UserIdFormatIsInvalid()
                throws Exception {
            // when & then
            mockMvc.perform(get("/api/channels")
                            .param(
                                    "userId",
                                    "invalid-uuid"
                            ))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널 수정")
    class Update {

        @Test
        @DisplayName("경로의 채널 ID와 요청값으로 채널을 수정하고 200 OK를 반환한다")
        void should_ReturnUpdatedChannel_when_UpdateRequestIsValid()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();
            Instant updatedAt =
                    Instant.parse("2026-07-28T03:00:00Z");

            ChannelUpdateRequest request =
                    new ChannelUpdateRequest(
                            null,
                            null,
                            "수정된 채널",
                            "수정된 설명"
                    );

            ChannelResponse response =
                    new ChannelResponse(
                            channelId,
                            Instant.parse(
                                    "2026-07-28T01:00:00Z"
                            ),
                            updatedAt,
                            ChannelType.PUBLIC,
                            "수정된 채널",
                            "수정된 설명",
                            null,
                            List.of()
                    );

            given(channelService.update(
                    any(ChannelUpdateRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_JSON
                    ))
                    .andExpect(jsonPath("$.id")
                            .value(channelId.toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("수정된 채널"))
                    .andExpect(jsonPath("$.description")
                            .value("수정된 설명"))
                    .andExpect(jsonPath("$.updatedAt")
                            .value(updatedAt.toString()));

            /*
             * Controller가 경로의 ID와 PUBLIC 타입을
             * 새 요청 객체에 넣었는지 검증한다.
             */
            then(channelService)
                    .should()
                    .update(argThat(fixedRequest ->
                            channelId.equals(fixedRequest.getId())
                                    && fixedRequest.getType()
                                    == ChannelType.PUBLIC
                                    && "수정된 채널".equals(
                                    fixedRequest.getName()
                            )
                                    && "수정된 설명".equals(
                                    fixedRequest.getDescription()
                            )
                    ));
        }

        @Test
        @DisplayName("수정할 이름과 설명이 모두 없으면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_UpdateValuesAreMissing()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            String requestJson = """
                    {}
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .update(any(ChannelUpdateRequest.class));
        }

        @Test
        @DisplayName("채널 이름이 공백으로만 구성되면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_UpdateChannelNameContainsOnlyWhitespace()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            String requestJson = """
                    {
                      "name": "   "
                    }
                    """;

            // when & then
            mockMvc.perform(patch(
                            "/api/channels/{channelId}",
                            channelId
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .should(never())
                    .update(any(ChannelUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("채널 삭제")
    class Delete {

        @Test
        @DisplayName("채널을 삭제하면 204 No Content를 반환한다")
        void should_ReturnNoContent_when_ChannelIsDeleted()
                throws Exception {
            // given
            UUID channelId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete(
                            "/api/channels/{channelId}",
                            channelId
                    ))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            then(channelService)
                    .should()
                    .delete(channelId);
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제하면 400 Bad Request를 반환한다")
        void should_ReturnBadRequest_when_DeleteChannelIdFormatIsInvalid()
                throws Exception {
            // when & then
            mockMvc.perform(delete(
                            "/api/channels/{channelId}",
                            "invalid-uuid"
                    ))
                    .andExpect(status().isBadRequest());

            then(channelService)
                    .shouldHaveNoInteractions();
        }
    }
}