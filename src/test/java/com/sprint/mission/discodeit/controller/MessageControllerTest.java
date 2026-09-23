package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import com.sprint.mission.discodeit.dto.request.ValidationMessage;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MessageController 슬라이스 테스트")
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MessageService messageService;

    @Test
    @DisplayName("메시지 생성 성공 - multipart 요청이면 201 Created와 메시지 정보 반환")
    void create_returnsCreatedAndMessage_whenMultipartRequestIsValid() throws Exception {
        // given
        // MessageController.create(...)는 @RequestPart를 사용한다.
        // 따라서 MessageCreateRequest JSON도 multipart의 "messageCreateRequest" part로 만들어야 한다.
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "test message",
                channelId,
                authorId
        );
        MockMultipartFile messageCreateRequestPart = getMessageCreateRequestPart(request);

        // attachments는 선택 part지만, 이 테스트는 파일이 함께 전달될 때
        // Controller가 List<MultipartFile>로 바인딩해 Service까지 넘기는지 확인한다.
        MockMultipartFile firstAttachmentPart = getAttachmentPart("first-file.png", MediaType.IMAGE_PNG_VALUE, "first");
        MockMultipartFile secondAttachmentPart = getAttachmentPart("second-file.txt", MediaType.TEXT_PLAIN_VALUE, "second");

        // Service가 반환할 MessageDto를 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 저장 로직이 아니라
        // multipart request 바인딩, HTTP 201 응답, JSON 직렬화, Service 호출 계약이다.
        UserDto author = getUserDto(authorId, "testUser", "test@gmail.com");
        BinaryContentDto firstAttachment = getBinaryContentDto(
                firstAttachmentPart.getOriginalFilename(),
                firstAttachmentPart.getSize(),
                firstAttachmentPart.getContentType()
        );
        BinaryContentDto secondAttachment = getBinaryContentDto(
                secondAttachmentPart.getOriginalFilename(),
                secondAttachmentPart.getSize(),
                secondAttachmentPart.getContentType()
        );
        UUID messageId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        MessageDto response = getMessageDto(
                messageId,
                createdAt,
                updatedAt,
                request.content(),
                channelId,
                author,
                List.of(firstAttachment, secondAttachment)
        );

        // Controller 내부에서 request.toCommand()가 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(messageService.save(any(MessageCreateCommand.class), any())).willReturn(response);

        // when
        // POST /api/messages 요청을 multipart/form-data로 전송한다.
        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequestPart)
                        .file(firstAttachmentPart)
                        .file(secondAttachmentPart)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 201 Created이고, Service가 반환한 MessageDto가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()))
                .andExpect(jsonPath("$.content").value(response.content()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.author.username").value(author.username()))
                .andExpect(jsonPath("$.author.email").value(author.email()))
                .andExpect(jsonPath("$.attachments.length()").value(2))
                .andExpect(jsonPath("$.attachments[0].id").value(firstAttachment.id().toString()))
                .andExpect(jsonPath("$.attachments[0].fileName").value(firstAttachment.fileName()))
                .andExpect(jsonPath("$.attachments[0].size").value(firstAttachment.size()))
                .andExpect(jsonPath("$.attachments[0].contentType").value(firstAttachment.contentType()))
                .andExpect(jsonPath("$.attachments[1].id").value(secondAttachment.id().toString()))
                .andExpect(jsonPath("$.attachments[1].fileName").value(secondAttachment.fileName()))
                .andExpect(jsonPath("$.attachments[1].size").value(secondAttachment.size()))
                .andExpect(jsonPath("$.attachments[1].contentType").value(secondAttachment.contentType()));

        // request part와 attachments part가 각각 command, MultipartFile 목록으로 변환되어 전달됐는지 확인한다.
        ArgumentCaptor<MessageCreateCommand> commandCaptor = ArgumentCaptor.forClass(MessageCreateCommand.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MultipartFile>> attachmentsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(messageService).save(commandCaptor.capture(), attachmentsCaptor.capture());

        MessageCreateCommand command = commandCaptor.getValue();
        assertThat(command.content()).isEqualTo(request.content());
        assertThat(command.userId()).isEqualTo(request.authorId());
        assertThat(command.channelId()).isEqualTo(request.channelId());

        List<MultipartFile> attachments = attachmentsCaptor.getValue();
        assertThat(attachments).hasSize(2);
        assertMultipartFile(attachments.get(0), firstAttachmentPart);
        assertMultipartFile(attachments.get(1), secondAttachmentPart);
    }

    @Test
    @DisplayName("메시지 생성 실패 - request part가 유효하지 않으면 400 Bad Request 반환")
    void create_returnsBadRequest_whenMessageCreateRequestIsInvalid() throws Exception {
        // given
        // MessageCreateRequest의 content, channelId, authorId는 모두 Bean Validation 대상이다.
        // 잘못된 request part를 보내면 Controller 본문에 진입하기 전에 검증이 실패해야 한다.
        MessageCreateRequest request = new MessageCreateRequest(
                " ",
                null,
                null
        );
        MockMultipartFile messageCreateRequestPart = getMessageCreateRequestPart(request);

        // when
        // POST /api/messages 요청을 multipart/form-data로 전송한다.
        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequestPart)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // GlobalExceptionHandler가 내려주는 validation 응답 구조와 실패 필드 메시지를 확인한다.
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 데이터가 올바르지 않습니다."))
                .andExpect(jsonPath("$.details.content").isArray())
                .andExpect(jsonPath("$.details.content[0]").value(ValidationMessage.MESSAGE_CONTENT))
                .andExpect(jsonPath("$.details.channelId").isArray())
                .andExpect(jsonPath("$.details.channelId[0]").value(ValidationMessage.CHANNEL_ID_MESSAGE))
                .andExpect(jsonPath("$.details.authorId").isArray())
                .andExpect(jsonPath("$.details.authorId[0]").value(ValidationMessage.USER_ID_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists());

        // validation 실패 요청은 MessageService까지 전달되면 안 된다.
        verify(messageService, never()).save(any(MessageCreateCommand.class), any());
    }

    @Test
    @DisplayName("채널별 메시지 목록 조회 성공 - channelId와 pageable, cursor를 바인딩해 200 OK 반환")
    void listByChannelId_returnsOkAndPageResponse_whenQueryParametersAreValid() throws Exception {
        // given
        // MessageController.listByChannelId(...)는 channelId query parameter, Pageable, cursor를 받는다.
        // Pageable은 page/size/sort query parameter로 바인딩되고, cursor는 UUID 문자열로 바인딩된다.
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        String nextCursor = UUID.randomUUID().toString();

        MessageDto message = getMessageDto(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-07-28T10:15:30+09:00"),
                OffsetDateTime.parse("2026-07-28T10:20:30+09:00"),
                "paged message",
                channelId,
                getUserDto(authorId, "testUser", "test@gmail.com"),
                List.of(getBinaryContentDto("message-file.png", 7L, MediaType.IMAGE_PNG_VALUE))
        );
        PageResponse<MessageDto> response = new PageResponse<>(
                List.of(message),
                nextCursor,
                2,
                true
        );

        given(messageService.findAllByChannelId(any(UUID.class), any(Pageable.class), any(UUID.class)))
                .willReturn(response);

        // when
        // GET /api/messages 요청을 query parameter와 함께 전송한다.
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString())
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "createdAt,desc")
                        .param("cursor", cursor.toString())
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고, PageResponse가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(message.id().toString()))
                .andExpect(jsonPath("$.content[0].content").value(message.content()))
                .andExpect(jsonPath("$.content[0].channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.content[0].author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.content[0].attachments.length()").value(1))
                .andExpect(jsonPath("$.nextCursor").value(nextCursor))
                .andExpect(jsonPath("$.size").value(response.size()))
                .andExpect(jsonPath("$.hasNext").value(response.hasNext()));

        // query parameter가 각각 UUID, Pageable, UUID로 바인딩되어 Service에 전달됐는지 확인한다.
        ArgumentCaptor<UUID> channelIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<UUID> cursorCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(messageService).findAllByChannelId(
                channelIdCaptor.capture(),
                pageableCaptor.capture(),
                cursorCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order createdAtOrder = pageable.getSort().getOrderFor("createdAt");
        assertThat(channelIdCaptor.getValue()).isEqualTo(channelId);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(2);
        assertThat(createdAtOrder).isNotNull();
        assertThat(createdAtOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(cursorCaptor.getValue()).isEqualTo(cursor);
    }

    @Test
    @DisplayName("메시지 수정 성공 - 유효한 요청이면 200 OK와 수정된 메시지 정보 반환")
    void update_returnsOkAndMessage_whenRequestIsValid() throws Exception {
        // given
        // MessageController.update(...)는 messageId path variable과 application/json request body를 받는다.
        // request.toCommand() 결과가 MessageService.update(...)에 전달되어야 한다.
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest("updated message");
        MessageDto response = getMessageDto(
                messageId,
                OffsetDateTime.parse("2026-07-28T10:15:30+09:00"),
                OffsetDateTime.parse("2026-07-28T10:30:30+09:00"),
                request.newContent(),
                channelId,
                getUserDto(authorId, "testUser", "test@gmail.com"),
                List.of()
        );

        given(messageService.update(any(UUID.class), any(MessageUpdateCommand.class))).willReturn(response);

        // when
        // PATCH /api/messages/{messageId} 요청을 application/json으로 전송한다.
        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                // 응답 상태가 200 OK이고, 수정된 MessageDto가 JSON으로 직렬화되는지 확인한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.createdAt").value(response.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(response.updatedAt().toString()))
                .andExpect(jsonPath("$.content").value(response.content()))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.attachments.length()").value(0));

        // path variable과 request body가 각각 messageId, MessageUpdateCommand로 변환되어 전달됐는지 확인한다.
        ArgumentCaptor<UUID> messageIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<MessageUpdateCommand> commandCaptor = ArgumentCaptor.forClass(MessageUpdateCommand.class);
        verify(messageService).update(messageIdCaptor.capture(), commandCaptor.capture());

        assertThat(messageIdCaptor.getValue()).isEqualTo(messageId);
        assertThat(commandCaptor.getValue().content()).isEqualTo(request.newContent());
    }

    @Test
    @DisplayName("메시지 삭제 성공 - messageId가 유효하면 204 No Content 반환")
    void delete_returnsNoContent_whenMessageIdIsValid() throws Exception {
        // given
        // path variable로 사용할 messageId를 준비한다.
        // messageService.delete(...)는 void 메서드이므로 정상 흐름에서 별도 stub이 필요하지 않다.
        UUID messageId = UUID.randomUUID();

        // when
        // DELETE /api/messages/{messageId} 요청을 전송한다.
        mockMvc.perform(delete("/api/messages/{messageId}", messageId)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 204 No Content이고 body가 비어 있는지 확인한다.
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // path variable이 UUID로 바인딩된 뒤 그대로 messageService.delete(...)에 전달되어야 한다.
        verify(messageService).delete(messageId);
    }

    private MockMultipartFile getMessageCreateRequestPart(MessageCreateRequest request) throws JsonProcessingException {
        return new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private MockMultipartFile getAttachmentPart(String fileName, String contentType, String content) {
        return new MockMultipartFile(
                "attachments",
                fileName,
                contentType,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MessageDto getMessageDto(
            UUID id,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String content,
            UUID channelId,
            UserDto author,
            List<BinaryContentDto> attachments
    ) {
        return new MessageDto(id, createdAt, updatedAt, content, channelId, author, attachments);
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

    private BinaryContentDto getBinaryContentDto(String fileName, Long size, String contentType) {
        return new BinaryContentDto(UUID.randomUUID(), fileName, size, contentType);
    }

    private void assertMultipartFile(MultipartFile actual, MockMultipartFile expected) throws Exception {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getOriginalFilename()).isEqualTo(expected.getOriginalFilename());
        assertThat(actual.getContentType()).isEqualTo(expected.getContentType());
        assertThat(actual.getSize()).isEqualTo(expected.getSize());
        assertThat(actual.getBytes()).isEqualTo(expected.getBytes());
    }

}
