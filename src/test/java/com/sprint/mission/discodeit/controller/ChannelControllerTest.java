package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePrivateCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.channel.ChannelUpdateCommand;
import com.sprint.mission.discodeit.dto.request.ValidationMessage;
import com.sprint.mission.discodeit.dto.request.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.ChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChannelController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChannelController 슬라이스 테스트")
class ChannelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ChannelService channelService;

    @Test
    @DisplayName("PUBLIC 채널 생성 성공 - 유효한 요청이면 201 Created와 채널 정보 반환")
    void createPublic_returnsCreatedAndChannel_whenRequestIsValid() throws Exception {
        // given
        // PUBLIC 채널 생성 endpoint는 application/json request body를 받는다.
        // Controller는 request.toCommand()로 ChannelCreatePublicCommand를 만든 뒤 ChannelService에 전달한다.
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "public-channel",
                "public channel description"
        );

        // Service가 반환할 ChannelDto를 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 저장 로직이 아니라
        // request body 바인딩, HTTP 201 응답, JSON 직렬화, Service 호출 계약이다.
        UUID channelId = UUID.randomUUID();
        OffsetDateTime lastMessageAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PUBLIC,
                request.name(),
                request.description(),
                List.of(),
                lastMessageAt
        );

        // Controller 내부에서 request.toCommand()가 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(channelService.save(any(ChannelCreateCommand.class))).willReturn(response);

        // when
        // POST /api/channels/public 요청을 application/json으로 전송한다.
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 201 Created이고, Service가 반환한 ChannelDto가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants.length()").value(0))
                .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()));

        // request body가 ChannelCreatePublicCommand로 변환되어 Service에 전달됐는지 확인한다.
        ArgumentCaptor<ChannelCreateCommand> commandCaptor = ArgumentCaptor.forClass(ChannelCreateCommand.class);
        verify(channelService).save(commandCaptor.capture());

        assertThat(commandCaptor.getValue()).isInstanceOf(ChannelCreatePublicCommand.class);
        ChannelCreatePublicCommand command = (ChannelCreatePublicCommand) commandCaptor.getValue();
        assertThat(command.channelName()).isEqualTo(request.name());
        assertThat(command.channelDescription()).isEqualTo(request.description());
        assertThat(command.channelType()).isEqualTo(ChannelType.PUBLIC);
        assertThat(command.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("PUBLIC 채널 생성 실패 - 이름이 비어 있으면 400 Bad Request 반환")
    void createPublic_returnsBadRequest_whenNameIsBlank() throws Exception {
        // given
        // PublicChannelCreateRequest.name은 @NotBlank 대상이다.
        // blank name을 보내면 Controller 본문에 진입하기 전에 Bean Validation이 실패해야 한다.
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                " ",
                "public channel description"
        );

        // when
        // POST /api/channels/public 요청을 application/json으로 전송한다.
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // GlobalExceptionHandler가 내려주는 validation 응답 구조와 name 필드 메시지를 확인한다.
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 데이터가 올바르지 않습니다."))
                .andExpect(jsonPath("$.details.name").isArray())
                .andExpect(jsonPath("$.details.name[0]").value(ValidationMessage.CHANNEL_NAME_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());

        // validation 실패 요청은 ChannelService까지 전달되면 안 된다.
        verify(channelService, never()).save(any(ChannelCreateCommand.class));
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 성공 - 참여자 목록이 유효하면 201 Created와 채널 정보 반환")
    void createPrivate_returnsCreatedAndChannel_whenRequestIsValid() throws Exception {
        // given
        // PRIVATE 채널 생성 endpoint는 서로 다른 참여자 UUID 2개 이상을 받는다.
        // Controller는 request.toCommand()로 ChannelCreatePrivateCommand를 만든 뒤 ChannelService에 전달한다.
        UUID firstParticipantId = UUID.randomUUID();
        UUID secondParticipantId = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(firstParticipantId, secondParticipantId)
        );

        UserDto firstParticipant = getUserDto(firstParticipantId, "firstUser", "first@gmail.com");
        UserDto secondParticipant = getUserDto(secondParticipantId, "secondUser", "second@gmail.com");
        UUID channelId = UUID.randomUUID();
        OffsetDateTime lastMessageAt = OffsetDateTime.parse("2026-07-28T11:15:30+09:00");
        ChannelDto response = new ChannelDto(
                channelId,
                ChannelType.PRIVATE,
                "private-channel",
                "private channel description",
                List.of(firstParticipant, secondParticipant),
                lastMessageAt
        );

        given(channelService.save(any(ChannelCreateCommand.class))).willReturn(response);

        // when
        // POST /api/channels/private 요청을 application/json으로 전송한다.
        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 201 Created이고, 참여자 목록을 포함한 ChannelDto JSON이 내려오는지 확인한다.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value(ChannelType.PRIVATE.name()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.participants.length()").value(2))
                .andExpect(jsonPath("$.participants[0].id").value(firstParticipantId.toString()))
                .andExpect(jsonPath("$.participants[0].username").value(firstParticipant.username()))
                .andExpect(jsonPath("$.participants[1].id").value(secondParticipantId.toString()))
                .andExpect(jsonPath("$.participants[1].username").value(secondParticipant.username()))
                .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()));

        // participantIds가 ChannelCreatePrivateCommand로 변환되어 Service에 전달됐는지 확인한다.
        ArgumentCaptor<ChannelCreateCommand> commandCaptor = ArgumentCaptor.forClass(ChannelCreateCommand.class);
        verify(channelService).save(commandCaptor.capture());

        assertThat(commandCaptor.getValue()).isInstanceOf(ChannelCreatePrivateCommand.class);
        ChannelCreatePrivateCommand command = (ChannelCreatePrivateCommand) commandCaptor.getValue();
        assertThat(command.participantIds()).containsExactly(firstParticipantId, secondParticipantId);
        assertThat(command.channelType()).isEqualTo(ChannelType.PRIVATE);
        assertThat(command.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 실패 - 참여자 목록이 유효하지 않으면 400 Bad Request 반환")
    void createPrivate_returnsBadRequest_whenParticipantIdsAreInvalid() throws Exception {
        // given
        // participantIds는 중복 UUID를 허용하지 않는다.
        // 같은 UUID를 두 번 보내면 @UniqueElements 검증이 실패해야 한다.
        UUID duplicatedParticipantId = UUID.randomUUID();
        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(duplicatedParticipantId, duplicatedParticipantId)
        );

        // when
        // POST /api/channels/private 요청을 application/json으로 전송한다.
        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // GlobalExceptionHandler가 내려주는 validation 응답 구조와 participantIds 메시지를 확인한다.
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 데이터가 올바르지 않습니다."))
                .andExpect(jsonPath("$.details.participantIds").isArray())
                .andExpect(jsonPath("$.details.participantIds[0]").value(ValidationMessage.USER_ID_UNIQUE_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());

        // validation 실패 요청은 ChannelService까지 전달되면 안 된다.
        verify(channelService, never()).save(any(ChannelCreateCommand.class));
    }

    @Test
    @DisplayName("사용자별 채널 목록 조회 성공 - userId query parameter가 있으면 200 OK와 채널 목록 반환")
    void listByUserId_returnsOkAndChannels_whenUserIdExists() throws Exception {
        // given
        // ChannelController.listByUserId(...)는 request body 없이 userId query parameter만 받는다.
        // GET 요청이므로 contentType은 지정하지 않고, 응답 타입만 accept로 지정한다.
        UUID userId = UUID.randomUUID();
        ChannelDto publicChannel = getChannelDto(
                UUID.randomUUID(),
                ChannelType.PUBLIC,
                "public-channel",
                "public description",
                List.of(),
                OffsetDateTime.parse("2026-07-28T10:15:30+09:00")
        );
        ChannelDto privateChannel = getChannelDto(
                UUID.randomUUID(),
                ChannelType.PRIVATE,
                "private-channel",
                "private description",
                List.of(getUserDto(userId, "testUser", "test@gmail.com")),
                OffsetDateTime.parse("2026-07-28T11:15:30+09:00")
        );

        // query parameter에서 바인딩된 userId가 Service로 전달되는지 captor로 검증할 것이므로
        // stub은 타입 기준으로 열어 둔다.
        given(channelService.findAllByUserId(any(UUID.class))).willReturn(List.of(publicChannel, privateChannel));

        // when
        // GET /api/channels?userId={userId} 요청을 전송한다.
        mockMvc.perform(get("/api/channels")
                        .param("userId", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고, Service가 반환한 순서대로 JSON 배열이 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(publicChannel.id().toString()))
                .andExpect(jsonPath("$[0].type").value(ChannelType.PUBLIC.name()))
                .andExpect(jsonPath("$[0].name").value(publicChannel.name()))
                .andExpect(jsonPath("$[0].description").value(publicChannel.description()))
                .andExpect(jsonPath("$[0].participants.length()").value(0))
                .andExpect(jsonPath("$[0].lastMessageAt").value(publicChannel.lastMessageAt().toString()))
                .andExpect(jsonPath("$[1].id").value(privateChannel.id().toString()))
                .andExpect(jsonPath("$[1].type").value(ChannelType.PRIVATE.name()))
                .andExpect(jsonPath("$[1].name").value(privateChannel.name()))
                .andExpect(jsonPath("$[1].description").value(privateChannel.description()))
                .andExpect(jsonPath("$[1].participants[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[1].lastMessageAt").value(privateChannel.lastMessageAt().toString()));

        ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(channelService).findAllByUserId(userIdCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    @DisplayName("채널 수정 성공 - 유효한 요청이면 200 OK와 수정된 채널 정보 반환")
    void update_returnsOkAndChannel_whenRequestIsValid() throws Exception {
        // given
        // ChannelController.update(...)는 channelId path variable과 application/json request body를 받는다.
        // request.toCommand() 결과가 ChannelService.update(...)에 전달되어야 한다.
        UUID channelId = UUID.randomUUID();
        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "updated-channel",
                "updated description"
        );
        OffsetDateTime lastMessageAt = OffsetDateTime.parse("2026-07-28T12:15:30+09:00");
        ChannelDto response = getChannelDto(
                channelId,
                ChannelType.PUBLIC,
                request.newName(),
                request.newDescription(),
                List.of(),
                lastMessageAt
        );

        given(channelService.update(any(UUID.class), any(ChannelUpdateCommand.class))).willReturn(response);

        // when
        // PATCH /api/channels/{channelId} 요청을 application/json으로 전송한다.
        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 200 OK이고, 수정된 ChannelDto가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.participants.length()").value(0))
                .andExpect(jsonPath("$.lastMessageAt").value(lastMessageAt.toString()));

        // path variable과 request body가 각각 channelId, ChannelUpdateCommand로 변환되어 전달됐는지 확인한다.
        ArgumentCaptor<UUID> channelIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<ChannelUpdateCommand> commandCaptor = ArgumentCaptor.forClass(ChannelUpdateCommand.class);
        verify(channelService).update(channelIdCaptor.capture(), commandCaptor.capture());

        assertThat(channelIdCaptor.getValue()).isEqualTo(channelId);
        assertThat(commandCaptor.getValue().channelName()).isEqualTo(request.newName());
        assertThat(commandCaptor.getValue().channelDescription()).isEqualTo(request.newDescription());
    }

    @Test
    @DisplayName("채널 삭제 성공 - channelId가 유효하면 204 No Content 반환")
    void delete_returnsNoContent_whenChannelIdIsValid() throws Exception {
        // given
        // path variable로 사용할 channelId를 준비한다.
        // channelService.delete(...)는 void 메서드이므로 정상 흐름에서 별도 stub이 필요하지 않다.
        UUID channelId = UUID.randomUUID();

        // when
        // DELETE /api/channels/{channelId} 요청을 전송한다.
        mockMvc.perform(delete("/api/channels/{channelId}", channelId)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 204 No Content이고 body가 비어 있는지 확인한다.
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // path variable이 UUID로 바인딩된 뒤 그대로 channelService.delete(...)에 전달되어야 한다.
        verify(channelService).delete(channelId);
    }

    private ChannelDto getChannelDto(
            UUID id,
            ChannelType type,
            String name,
            String description,
            List<UserDto> participants,
            OffsetDateTime lastMessageAt
    ) {
        return new ChannelDto(id, type, name, description, participants, lastMessageAt);
    }

    private UserDto getUserDto(UUID id, String username, String email) {
        return new UserDto(
                id,
                username,
                email,
                null,
                true,
                Role.USER,
                OffsetDateTime.parse("2026-07-28T09:15:30+09:00"),
                OffsetDateTime.parse("2026-07-28T09:20:30+09:00")
        );
    }

}
