package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStatusService userStatusService;


    @Test
    @DisplayName("GET /api/users - 사용자 목록 조회 성공 시 200과 목록을 반환한다")
    void findAll_성공() throws Exception {
        // given: UserService.findAll()이 2명의 사용자를 반환하도록 설정
        UserDto user1 = new UserDto(UUID.randomUUID(), "user1", "user1@test.com", null, true);
        UserDto user2 = new UserDto(UUID.randomUUID(), "user2", "user2@test.com", null, false);
        given(userService.findAll()).willReturn(List.of(user1, user2));

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                // JSON 배열의 첫 번째 요소 검증
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].online").value(true))
                .andExpect(jsonPath("$[1].username").value("user2"))
                // 배열 크기 검증
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/users - 사용자가 없으면 빈 배열을 반환한다")
    void findAll_빈목록() throws Exception {
        // given
        given(userService.findAll()).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @DisplayName("DELETE /api/users/{userId} - 삭제 성공 시 204를 반환한다")
    void delete_성공() throws Exception {
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 존재하지 않는 사용자면 404와 에러 응답을 반환한다")
    void delete_실패_사용자없음() throws Exception {
        // given
        UUID notExistUserId = UUID.randomUUID();
        willThrow(new UserNotFoundException(notExistUserId))
                .given(userService).delete(notExistUserId);

        // when & then: 404 + ErrorResponse 형식 검증
        mockMvc.perform(delete("/api/users/{userId}", notExistUserId))
                .andExpect(status().isNotFound())
                // GlobalExceptionHandler가 ErrorResponse로 변환했는지 검증
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.details.userId").value(notExistUserId.toString()));
    }
}