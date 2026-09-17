package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.request.ValidationMessage;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;


    @Test
    @DisplayName("사용자 생성 성공 - multipart 요청이면 200 OK와 사용자 정보 반환")
    void create_returnsOkAndUser_whenMultipartRequestIsValid() throws Exception {
        // given
        // UserController.create(...)는 @RequestBody가 아니라 @RequestPart를 사용한다.
        // 따라서 UserCreateRequest JSON도 multipart의 한 part로 만들어야 한다.
        // part name은 Controller 파라미터 이름인 "userCreateRequest"와 정확히 같아야 한다.
        UserCreateRequest request = getUserCreateRequest();
        MockMultipartFile userCreateRequestPart = getUserRequestPart(request);

        // profile은 선택 part지만, 이 테스트는 파일을 함께 보냈을 때
        // Controller가 MultipartFile을 Service까지 그대로 전달하는지 확인한다.
        // part name 역시 Controller의 @RequestPart 이름인 "profile"과 같아야 한다.
        MockMultipartFile profilePart = getMultipartFile();

        // Service가 반환할 응답 DTO를 실제 record로 구성한다.
        // Controller 슬라이스 테스트의 관심사는 DTO 생성 로직이 아니라 HTTP 응답 직렬화이므로,
        // UserService는 Mock으로 두고 반환값만 명시한다.
        UUID userId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        BinaryContentDto profileDto = getProfile(profilePart);
        UUID profileId = profileDto.id();
        UserDto response = getResponse(userId, request, profileDto, createdAt, updatedAt);

        // request.toCommand()는 Controller 내부에서 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 넓게 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(userService.create(any(UserCreateCommand.class), any(MultipartFile.class))).willReturn(response);

        // when
        // JSON request part와 file part를 함께 담아 multipart/form-data 요청을 보낸다.
        // multipart(...) builder가 multipart content-type을 구성하므로 별도의 raw .content(...)는 넣지 않는다.
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequestPart)
                        .file(profilePart)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // Controller가 200 OK를 반환하고, Service가 반환한 UserDto가 JSON으로 직렬화되는지 확인한다.
                // profile은 중첩 객체이므로 id뿐 아니라 파일명, 크기, contentType까지 같이 검증한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(response.username()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.profile.id").value(profileId.toString()))
                .andExpect(jsonPath("$.profile.fileName").value(profileDto.fileName()))
                .andExpect(jsonPath("$.profile.size").value(profileDto.size()))
                .andExpect(jsonPath("$.profile.contentType").value(profileDto.contentType()))
                .andExpect(jsonPath("$.online").value(response.online()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

        // Controller가 UserCreateRequest를 command로 변환해 Service에 넘겼는지 확인한다.
        // MultipartFile은 같은 part의 메타데이터와 byte content가 유지됐는지 확인한다.
        ArgumentCaptor<UserCreateCommand> commandCaptor = ArgumentCaptor.forClass(UserCreateCommand.class);
        ArgumentCaptor<MultipartFile> profileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(userService).create(commandCaptor.capture(), profileCaptor.capture());

        assertThat(commandCaptor.getValue().username()).isEqualTo(request.username());
        assertThat(commandCaptor.getValue().password()).isEqualTo(request.password());
        assertThat(commandCaptor.getValue().email()).isEqualTo(request.email());

        MultipartFile capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile.getOriginalFilename()).isEqualTo(profilePart.getOriginalFilename());
        assertThat(capturedProfile.getContentType()).isEqualTo(profilePart.getContentType());
        assertThat(capturedProfile.getSize()).isEqualTo(profilePart.getSize());
        assertThat(capturedProfile.getBytes()).isEqualTo(profilePart.getBytes());
    }

    @Test
    @DisplayName("사용자 생성 실패 - multipart request part가 유효하지 않으면 400 Bad Request 반환")
    void create_returnsBadRequest_whenUserCreateRequestIsInvalid() throws Exception {
        // given
        // 이 테스트는 MultipartFile 검증이 아니라 UserCreateRequest part의 Bean Validation 실패를 검증한다.
        // profile part는 @RequestPart(required = false)이므로 아예 보내지 않는다.
        // 그래야 실패 원인이 선택 파일 part가 아니라 JSON request part의 필수값/형식 위반임이 분명해진다.
        UserCreateRequest request = getUserCreateRequest(
                "",
                " ",
                "invalid-email"
        );
        MockMultipartFile userCreateRequestPart = getUserRequestPart(request);

        // when
        // UserCreateRequest JSON을 "userCreateRequest" 이름의 multipart part로 전송한다.
        // @RequestPart 바인딩에서는 raw body .content(...)를 넣지 않고, JSON part 자체의 content-type을 application/json으로 둔다.
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequestPart)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // @Valid 검증이 Controller 본문 실행 전에 실패하므로 400 Bad Request가 반환되어야 한다.
                // GlobalExceptionHandler는 field name을 details의 key로 내려주므로 실패 필드별 메시지 배열을 확인한다.
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 데이터가 올바르지 않습니다."))
                .andExpect(jsonPath("$.details.username").isArray())
                .andExpect(jsonPath("$.details.username[0]").value(ValidationMessage.USER_NAME))
                .andExpect(jsonPath("$.details.password").isArray())
                .andExpect(jsonPath("$.details.password[0]").value(ValidationMessage.USER_PASSWORD))
                .andExpect(jsonPath("$.details.email").isArray())
                .andExpect(jsonPath("$.details.email[0]").value(ValidationMessage.USER_EMAIL))
                .andExpect(jsonPath("$.timestamp").exists());

        // validation 실패 요청은 UserController.create(...) 본문까지 도달하지 않아야 한다.
        // 따라서 UserService.create(...)는 물론 UserService와의 어떤 상호작용도 없어야 한다.
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 200 OK와 사용자 목록 반환")
    void list_returnsOkAndUsers() throws Exception {
        // given
        // UserController.list()는 request body, query parameter, multipart part를 받지 않는 단순 GET 엔드포인트다.
        // 따라서 이 테스트에서는 multipart fixture를 만들지 않고, Service가 반환할 UserDto 목록만 준비한다.
        //
        // Controller 슬라이스 테스트의 관심사는 UserService.findAll() 내부 조회 로직이 아니라
        // HTTP 200 응답, JSON 배열 직렬화, Service 호출 계약이다.
        UUID firstUserId = UUID.randomUUID();
        OffsetDateTime firstCreatedAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime firstUpdatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        UserCreateRequest firstRequest = getUserCreateRequest(
                "testUserName",
                "testPassword",
                "test@gmail.com"
        );
        BinaryContentDto firstProfile = getProfile(
                "test-profile.png",
                7L,
                MediaType.IMAGE_PNG_VALUE
        );
        UserDto firstResponse = getResponse(
                firstUserId,
                firstRequest,
                firstProfile,
                firstCreatedAt,
                firstUpdatedAt
        );

        // 두 번째 사용자를 함께 반환하게 해 배열 응답임을 더 분명히 검증한다.
        // profile을 null로 둬도 Controller는 Service 결과를 그대로 직렬화해야 한다.
        UUID secondUserId = UUID.randomUUID();
        OffsetDateTime secondCreatedAt = OffsetDateTime.parse("2026-07-28T11:15:30+09:00");
        OffsetDateTime secondUpdatedAt = OffsetDateTime.parse("2026-07-28T11:20:30+09:00");
        UserCreateRequest secondRequest = getUserCreateRequest(
                "otherUserName",
                "otherPassword",
                "other@gmail.com"
        );
        UserDto secondResponse = getResponse(
                secondUserId,
                secondRequest,
                null,
                secondCreatedAt,
                secondUpdatedAt
        );

        given(userService.findAll()).willReturn(List.of(firstResponse, secondResponse));

        // when
        // GET /api/users 요청을 보낸다.
        // GET 요청에는 body가 없으므로 contentType은 지정하지 않고, 받을 응답 타입만 accept로 지정한다.
        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 200 OK이고 body가 JSON 배열인지 확인한다.
                // 목록의 순서는 Controller가 임의로 정렬하지 않고 Service 반환 순서를 유지해야 한다.
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstUserId.toString()))
                .andExpect(jsonPath("$[0].username").value(firstResponse.username()))
                .andExpect(jsonPath("$[0].email").value(firstResponse.email()))
                .andExpect(jsonPath("$[0].profile.id").value(firstProfile.id().toString()))
                .andExpect(jsonPath("$[0].profile.fileName").value(firstProfile.fileName()))
                .andExpect(jsonPath("$[0].profile.size").value(firstProfile.size()))
                .andExpect(jsonPath("$[0].profile.contentType").value(firstProfile.contentType()))
                .andExpect(jsonPath("$[0].online").value(firstResponse.online()))
                .andExpect(jsonPath("$[0].createdAt").value(firstCreatedAt.toString()))
                .andExpect(jsonPath("$[0].updatedAt").value(firstUpdatedAt.toString()))
                .andExpect(jsonPath("$[1].id").value(secondUserId.toString()))
                .andExpect(jsonPath("$[1].username").value(secondResponse.username()))
                .andExpect(jsonPath("$[1].email").value(secondResponse.email()))
                .andExpect(jsonPath("$[1].profile").doesNotExist())
                .andExpect(jsonPath("$[1].online").value(secondResponse.online()))
                .andExpect(jsonPath("$[1].createdAt").value(secondCreatedAt.toString()))
                .andExpect(jsonPath("$[1].updatedAt").value(secondUpdatedAt.toString()));

        // 목록 조회 endpoint는 UserService.findAll()을 호출해야 한다.
        verify(userService).findAll();
    }

    @Test
    @DisplayName("사용자 수정 성공 - multipart 요청이면 200 OK와 수정된 사용자 정보 반환")
    void update_returnsOkAndUser_whenMultipartRequestIsValid() throws Exception {
        // given
        // UserController.update(...)도 create(...)와 마찬가지로 @RequestPart를 사용한다.
        // JSON part 이름은 Controller 파라미터 이름인 "userUpdateRequest"와 정확히 같아야 한다.
        UserUpdateRequest request = getUserUpdateRequest();
        MockMultipartFile userUpdateRequestPart = getUserRequestPart(request);

        // profile은 수정 요청에서 선택 part지만, 이 테스트는 새 프로필 파일을 함께 보냈을 때
        // Controller가 MultipartFile을 Service까지 전달하는지 확인한다.
        MockMultipartFile profilePart = getMultipartFile();
        BinaryContentDto profileDto = getProfile(profilePart);
        UUID profileId = profileDto.id();

        // path variable로 사용할 userId와 Service가 반환할 수정 결과 DTO를 준비한다.
        // Controller 슬라이스 테스트에서는 실제 수정 로직이 아니라 HTTP 바인딩과 응답 직렬화를 검증하므로
        // UserService.update(...)는 Mock으로 대체하고 반환값만 명시한다.
        UUID userId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-28T10:20:30+09:00");
        UserDto response = getResponse(userId, request, profileDto, createdAt, updatedAt);

        // Controller 내부에서 request.toCommand()를 호출해 새 command 인스턴스를 만들기 때문에
        // stub은 타입 기준으로 열고, 실제 전달값은 아래 ArgumentCaptor로 검증한다.
        given(userService.update(any(UUID.class), any(UserUpdateCommand.class), any(MultipartFile.class)))
                .willReturn(response);

        // 현재 테스트에서는 create/list와 같은 응답 필드 계약을 그대로 확인한다.
        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .file(userUpdateRequestPart)
                        .file(profilePart)
                        .with(servletRequest -> {
                            servletRequest.setMethod("PATCH");
                            return servletRequest;
                        })
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(response.username()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.profile.id").value(profileId.toString()))
                .andExpect(jsonPath("$.profile.fileName").value(profileDto.fileName()))
                .andExpect(jsonPath("$.profile.size").value(profileDto.size()))
                .andExpect(jsonPath("$.profile.contentType").value(profileDto.contentType()))
                .andExpect(jsonPath("$.online").value(response.online()))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

        ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UserUpdateCommand> commandCaptor = ArgumentCaptor.forClass(UserUpdateCommand.class);
        ArgumentCaptor<MultipartFile> profileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(userService).update(userIdCaptor.capture(), commandCaptor.capture(), profileCaptor.capture());

        // path variable, request part, file part가 모두 Service 인자로 보존됐는지 확인한다.
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(commandCaptor.getValue().username()).isEqualTo(request.newUsername());
        assertThat(commandCaptor.getValue().password()).isEqualTo(request.newPassword());
        assertThat(commandCaptor.getValue().email()).isEqualTo(request.newEmail());

        MultipartFile capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile.getOriginalFilename()).isEqualTo(profilePart.getOriginalFilename());
        assertThat(capturedProfile.getContentType()).isEqualTo(profilePart.getContentType());
        assertThat(capturedProfile.getSize()).isEqualTo(profilePart.getSize());
        assertThat(capturedProfile.getBytes()).isEqualTo(profilePart.getBytes());

    }

    @Test
    @DisplayName("사용자 삭제 성공 - 사용자 ID가 유효하면 204 No Content 반환")
    void delete_returnsNoContent_whenUserIdIsValid() throws Exception {
        // given
        // path variable로 사용할 userId를 준비한다.
        // UserController.delete(...)는 request body를 받지 않고, userId만 Service에 전달한다.
        UUID userId = UUID.randomUUID();

        // userService.delete(...)는 void 메서드다.
        // 정상 흐름에서는 별도 stub을 만들지 않아도 Mock은 아무 동작 없이 반환한다.
        // 이 테스트의 관심사는 삭제 비즈니스 로직이 아니라 Controller의 HTTP status와 Service 호출 계약이다.

        // when
        // DELETE /api/users/{userId} 요청을 전송한다.
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .accept(MediaType.APPLICATION_JSON))

                // then
                // 응답 상태가 204 No Content인지 확인한다.
                // 응답 body가 비어 있는지 확인한다.
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // path variable이 UUID로 바인딩된 뒤 그대로 userService.delete(...)에 전달되어야 한다.
        verify(userService).delete(userId);

    }

    private MockMultipartFile getMultipartFile(String request, String fileName, String mediaType, byte[] bytes) {
        return new MockMultipartFile(request, fileName, mediaType, bytes);
    }

    private BinaryContentDto getProfile(MockMultipartFile file) {
        return getProfile(file.getOriginalFilename(), file.getSize(), file.getContentType());
    }

    private BinaryContentDto getProfile(String fileName, Long size, String contentType) {
        return new BinaryContentDto(UUID.randomUUID(), fileName, size, contentType);
    }

    private MockMultipartFile getMultipartFile() {
        return getMultipartFile(
                "profile",
                "test-profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "profile".getBytes(StandardCharsets.UTF_8)
        );
    }

    private UserCreateRequest getUserCreateRequest() {
        return getUserCreateRequest("testUserName", "testPassword", "test@gamil.com");
    }

    private UserCreateRequest getUserCreateRequest(String username, String password, String email) {
        return new UserCreateRequest(username, password, email);
    }

    private MockMultipartFile getUserRequestPart(UserCreateRequest request) throws JsonProcessingException {
        return getMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private MockMultipartFile getUserRequestPart(UserUpdateRequest request) throws JsonProcessingException {
        return getMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private static UserDto getResponse(UUID userId, UserCreateRequest request, BinaryContentDto profileDto, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new UserDto(
                userId,
                request.username(),
                request.email(),
                profileDto,
                true,
                Role.USER,
                createdAt,
                updatedAt
        );
    }

    private UserDto getResponse(UUID userId, UserUpdateRequest request, BinaryContentDto profileDto, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new UserDto(
                userId,
                request.newUsername(),
                request.newEmail(),
                profileDto,
                true,
                Role.USER,
                createdAt,
                updatedAt
        );
    }

    private UserUpdateRequest getUserUpdateRequest() {
        return getUserUpdateRequest("changeUser", "changePassword", "changeEmail@gmail.com");
    }

    private UserUpdateRequest getUserUpdateRequest(String username, String password, String email) {
        return new UserUpdateRequest(username, password, email);
    }

}
