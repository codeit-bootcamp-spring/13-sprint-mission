package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createUserAndGetResponse(String username, String email) throws Exception {
        MockMultipartFile userRequest = new MockMultipartFile(
                "userCreateRequest",   // @RequestPart의 파트 이름
                "",                    // 원본 파일명 (JSON 파트이므로 빈 문자열)
                "application/json",    // Content-Type: JSON으로 명시해야 @RequestPart가 파싱 가능
                objectMapper.writeValueAsBytes(new UserCreateRequest(username, email, "password1234"))
        );

        MvcResult result = mockMvc.perform(multipart("/api/users").file(userRequest))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString();
    }


    @Test
    @DisplayName("사용자 생성 성공 - 유효한 요청이면 201과 생성된 사용자 정보를 반환한다")
    void createUser_성공() throws Exception {
        // given & when
        String response = createUserAndGetResponse("testuser", "test@test.com");

        // then: 응답에 올바른 사용자 정보가 포함됐는지 검증
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 중복 이메일이면 409를 반환한다")
    void createUser_실패_중복이메일() throws Exception {
        // given: 첫 번째 사용자 생성
        createUserAndGetResponse("user1", "duplicate@test.com");

        // when & then
        MockMultipartFile duplicateRequest = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new UserCreateRequest("user2", "duplicate@test.com", "password"))
        );

        mockMvc.perform(multipart("/api/users").file(duplicateRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_DUPLICATE"));
    }


    @Test
    @DisplayName("사용자 목록 조회 성공 - 생성된 사용자 목록을 반환한다")
    void findAllUsers_성공() throws Exception {
        // given: 사용자 2명 생성
        createUserAndGetResponse("user1", "user1@test.com");
        createUserAndGetResponse("user2", "user2@test.com");

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("사용자 목록 조회 - 사용자가 없으면 빈 배열을 반환한다")
    void findAllUsers_빈목록() throws Exception {
        // when & then: 아무도 없으면 빈 배열
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @DisplayName("사용자 수정 성공 - 존재하는 사용자의 정보를 수정하면 200을 반환한다")
    void updateUser_성공() throws Exception {
        // given: 사용자 생성 후 ID 추출
        String createResponse = createUserAndGetResponse("olduser", "old@test.com");
        UUID userId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        // when: 사용자 정보 수정
        MockMultipartFile updateRequest = new MockMultipartFile(
                "userUpdateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new UserUpdateRequest("newuser", "new@test.com", "newpassword"))
        );

        // then
        mockMvc.perform(
                        multipart(HttpMethod.PATCH, "/api/users/{userId}", userId).file(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }


    @Test
    @DisplayName("사용자 삭제 성공 - 존재하는 사용자를 삭제하면 204를 반환한다")
    void deleteUser_성공() throws Exception {
        // given: 사용자 생성
        String createResponse = createUserAndGetResponse("deleteuser", "delete@test.com");
        UUID userId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자면 404를 반환한다")
    void deleteUser_실패_사용자없음() throws Exception {
        // given: 존재하지 않는 UUID
        UUID notExistId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", notExistId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}