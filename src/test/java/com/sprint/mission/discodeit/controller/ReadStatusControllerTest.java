package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.readStatus.ReadStatusUpdateCommand;
import com.sprint.mission.discodeit.dto.request.ValidationMessage;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.service.basic.ReadStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ReadStatusController 슬라이스 테스트")
class ReadStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReadStatusService readStatusService;

    @Test
    @DisplayName("읽음 상태 생성 성공 - 유효한 요청이면 201 Created와 읽음 상태 반환")
    void save_returnsCreatedAndReadStatus_whenRequestIsValid() throws Exception {
        // given
        // ReadStatusController.save(...)는 application/json request body를 받는다.
        // request 안의 channelId는 Service의 첫 번째 인자로 전달되고,
        // userId와 lastReadAt은 request.toCommand()를 거쳐 ReadStatusCreateCommand로 전달된다.
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant lastReadAt = Instant.parse("2026-07-28T01:25:30Z");
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                userId,
                channelId,
                lastReadAt
        );

        // Service가 반환할 ReadStatusDto를 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 저장 로직이 아니라
        // request body 바인딩, HTTP status, JSON 직렬화, Service 호출 계약이다.
        UUID readStatusId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        OffsetDateTime responseLastReadAt = OffsetDateTime.parse("2026-07-28T10:25:30+09:00");
        ReadStatusDto readStatusDto = new ReadStatusDto(
                readStatusId,
                createdAt,
                updatedAt,
                userId,
                channelId,
                responseLastReadAt
        );

        // Controller 내부에서 request.toCommand()가 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(readStatusService.save(any(UUID.class), any(ReadStatusCreateCommand.class)))
                .willReturn(readStatusDto);

        // when
        // POST /api/readStatuses 요청을 application/json으로 전송한다.
        mockMvc.perform(post("/api/readStatuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 201 Created이고, Service가 반환한 ReadStatusDto가 JSON으로 직렬화되는지 확인한다.
                // ReadStatusDto의 식별자, 연관 id, 시간 필드가 응답 body에 그대로 포함되어야 한다.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(readStatusId.toString()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.lastReadAt").value(responseLastReadAt.toString()));

        // request body의 channelId가 Service의 첫 번째 인자로 전달됐는지 확인한다.
        // request body의 userId와 lastReadAt은 ReadStatusCreateCommand로 변환되어 전달되어야 한다.
        ArgumentCaptor<UUID> channelIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<ReadStatusCreateCommand> readStatusCreateCommandArgumentCaptor = ArgumentCaptor.forClass(ReadStatusCreateCommand.class);
        verify(readStatusService).save(channelIdArgumentCaptor.capture(), readStatusCreateCommandArgumentCaptor.capture());

        UUID capturedChannelId = channelIdArgumentCaptor.getValue();
        ReadStatusCreateCommand capturedReadStatusCreateCommand = readStatusCreateCommandArgumentCaptor.getValue();
        assertThat(capturedChannelId).isEqualTo(channelId);
        assertThat(capturedReadStatusCreateCommand.userId()).isEqualTo(userId);
        assertThat(capturedReadStatusCreateCommand.readAt()).isEqualTo(lastReadAt);
    }

    @Test
    @DisplayName("읽음 상태 생성 실패 - 필수값이 없으면 400 Bad Request 반환")
    void save_returnsBadRequest_whenRequestIsInvalid() throws Exception {
        // given
        // 이 테스트는 ReadStatusCreateRequest의 Bean Validation 실패를 검증한다.
        // userId, channelId, lastReadAt은 모두 @NotNull 대상이므로 null로 보내면
        // Controller 본문에 진입하기 전에 MethodArgumentNotValidException이 발생해야 한다.
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                null,
                null,
                null
        );

        // when
        // POST /api/readStatuses 요청을 application/json body와 함께 전송한다.
        mockMvc.perform(post("/api/readStatuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // Bean Validation 실패로 400 Bad Request가 반환되는지 확인한다.
                // GlobalExceptionHandler는 실패 필드명을 details의 key로 내려주고 메시지를 배열로 담으므로,
                // 각 필드가 의도한 validation message를 갖는지 함께 검증한다.
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 데이터가 올바르지 않습니다."))
                .andExpect(jsonPath("$.details.userId").isArray())
                .andExpect(jsonPath("$.details.userId[0]").value(ValidationMessage.USER_ID_MESSAGE))
                .andExpect(jsonPath("$.details.channelId").isArray())
                .andExpect(jsonPath("$.details.channelId[0]").value(ValidationMessage.CHANNEL_ID_MESSAGE))
                .andExpect(jsonPath("$.details.lastReadAt").isArray())
                .andExpect(jsonPath("$.details.lastReadAt[0]").value(ValidationMessage.TIME_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());

        // validation 실패 요청은 ReadStatusController.save(...) 본문까지 도달하지 않아야 한다.
        // 따라서 readStatusService.save(...)가 호출되면 안 된다.
        verify(readStatusService, never()).save(any(UUID.class), any(ReadStatusCreateCommand.class));
    }

    @Test
    @DisplayName("사용자별 읽음 상태 목록 조회 성공 - userId query parameter가 있으면 200 OK와 목록 반환")
    void listByUserId_returnsOkAndReadStatuses_whenUserIdExists() throws Exception {
        // given
        // ReadStatusController.listByUserId(...)는 request body 없이 userId query parameter만 받는다.
        // 따라서 GET 요청에는 contentType을 지정하지 않고, 응답으로 받을 타입만 accept로 지정한다.
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID otherChannelId = UUID.randomUUID();

        // Service가 반환할 ReadStatusDto 목록을 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 조회 로직이 아니라
        // query parameter 바인딩, HTTP 200 응답, JSON 배열 직렬화, Service 호출 계약이다.
        UUID firstReadStatusId = UUID.randomUUID();
        OffsetDateTime firstCreatedAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime firstUpdatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        OffsetDateTime firstLastReadAt = OffsetDateTime.parse("2026-07-28T10:25:30+09:00");
        ReadStatusDto firstReadStatus = new ReadStatusDto(
                firstReadStatusId,
                firstCreatedAt,
                firstUpdatedAt,
                userId,
                channelId,
                firstLastReadAt
        );

        // 같은 사용자에게 여러 채널의 읽음 상태가 있을 수 있으므로 두 번째 row를 함께 반환한다.
        // 이 구성이 있어야 Controller가 Service 반환 목록을 배열 순서 그대로 직렬화하는지 확인할 수 있다.
        UUID secondReadStatusId = UUID.randomUUID();
        OffsetDateTime secondCreatedAt = OffsetDateTime.parse("2026-07-28T11:15:30+09:00");
        OffsetDateTime secondUpdatedAt = OffsetDateTime.parse("2026-07-28T11:20:30+09:00");
        OffsetDateTime secondLastReadAt = OffsetDateTime.parse("2026-07-28T11:25:30+09:00");
        ReadStatusDto secondReadStatus = new ReadStatusDto(
                secondReadStatusId,
                secondCreatedAt,
                secondUpdatedAt,
                userId,
                otherChannelId,
                secondLastReadAt
        );

        List<ReadStatusDto> readStatuses = List.of(firstReadStatus, secondReadStatus);

        // query parameter에서 바인딩된 userId가 Service로 전달되는지 아래 captor로 검증할 것이므로,
        // stub은 타입 기준으로 열어 둔다.
        given(readStatusService.findAllByUserId(any(UUID.class))).willReturn(readStatuses);

        // when
        // GET /api/readStatuses?userId={userId} 요청을 전송한다.
        mockMvc.perform(get("/api/readStatuses")
                        .param("userId", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고 body가 JSON 배열인지 확인한다.
                // Controller가 별도 가공 없이 Service 반환 순서를 유지해 내려주는지도 함께 검증한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstReadStatusId.toString()))
                .andExpect(jsonPath("$[0].createdAt").value(firstCreatedAt.toString()))
                .andExpect(jsonPath("$[0].updatedAt").value(firstUpdatedAt.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].channelId").value(channelId.toString()))
                .andExpect(jsonPath("$[0].lastReadAt").value(firstLastReadAt.toString()))
                .andExpect(jsonPath("$[1].id").value(secondReadStatusId.toString()))
                .andExpect(jsonPath("$[1].createdAt").value(secondCreatedAt.toString()))
                .andExpect(jsonPath("$[1].updatedAt").value(secondUpdatedAt.toString()))
                .andExpect(jsonPath("$[1].userId").value(userId.toString()))
                .andExpect(jsonPath("$[1].channelId").value(otherChannelId.toString()))
                .andExpect(jsonPath("$[1].lastReadAt").value(secondLastReadAt.toString()));

        // query parameter 문자열이 UUID로 바인딩된 뒤
        // readStatusService.findAllByUserId(...)에 그대로 전달되어야 한다.
        ArgumentCaptor<UUID> userIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(readStatusService).findAllByUserId(userIdArgumentCaptor.capture());

        assertThat(userIdArgumentCaptor.getValue()).isEqualTo(userId);
    }

    @Test
    @DisplayName("읽음 상태 수정 성공 - 유효한 요청이면 200 OK와 수정된 읽음 상태 반환")
    void update_returnsOkAndReadStatus_whenRequestIsValid() throws Exception {
        // given
        // ReadStatusController.update(...)는 readStatusId path variable과
        // application/json request body를 함께 받는다.
        // request의 newLastReadAt은 request.toCommand()를 거쳐 ReadStatusUpdateCommand.readAt으로 전달된다.
        UUID readStatusId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant lastReadAt = Instant.parse("2026-07-28T01:30:30Z");
        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(lastReadAt);

        // Service가 반환할 수정 결과 DTO를 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 수정 로직이 아니라
        // path variable/body 바인딩, HTTP 200 응답, JSON 직렬화, Service 호출 계약이다.
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-28T10:25:30+09:00");
        OffsetDateTime newLastReadAt = OffsetDateTime.parse("2026-07-28T10:30:30+09:00");
        ReadStatusDto readStatus = new ReadStatusDto(
                readStatusId,
                createdAt,
                updatedAt,
                userId,
                channelId,
                newLastReadAt
        );

        // Controller 내부에서 request.toCommand()가 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(readStatusService.update(any(UUID.class), any(ReadStatusUpdateCommand.class))).willReturn(readStatus);

        // when
        // PATCH /api/readStatuses/{readStatusId} 요청을 application/json으로 전송한다.
        mockMvc.perform(patch("/api/readStatuses/{readStatusId}", readStatusId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 200 OK이고, Service가 반환한 ReadStatusDto가 JSON으로 직렬화되는지 확인한다.
                // 수정 후 lastReadAt을 포함한 주요 응답 필드가 그대로 내려와야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(readStatusId.toString()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.lastReadAt").value(newLastReadAt.toString()));

        // path variable과 request body가 각각 readStatusId, ReadStatusUpdateCommand로 변환되어
        // readStatusService.update(...)에 전달됐는지 확인한다.
        ArgumentCaptor<UUID> readStatusIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<ReadStatusUpdateCommand> readStatusUpdateCommandArgumentCaptor = ArgumentCaptor.forClass(ReadStatusUpdateCommand.class);

        verify(readStatusService).update(readStatusIdArgumentCaptor.capture(), readStatusUpdateCommandArgumentCaptor.capture());
        assertThat(readStatusIdArgumentCaptor.getValue()).isEqualTo(readStatusId);
        assertThat(readStatusUpdateCommandArgumentCaptor.getValue().readAt()).isEqualTo(lastReadAt);
    }

}
