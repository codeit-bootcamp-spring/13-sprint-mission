package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    // 1. 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public User registerUser(@RequestBody UserCreateRequest request) {
        // 프로필 파일 첨부가 없는 기본 요청으로 처리 (필요시 수정 가능)
        return userService.create(request, Optional.empty());
    }

    // 2. 사용자 정보 수정
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public User updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        return userService.update(userId, request, Optional.empty());
    }

    // 3. 사용자 삭제
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    // 4. 모든 사용자 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<UserDto> getAllUsers() {
        return userService.findAll();
    }

    // 5. 사용자의 온라인 상태 업데이트
    @RequestMapping(value = "/{userId}/status", method = RequestMethod.PUT)
    public UserStatus updateUserStatus(@PathVariable UUID userId, @RequestBody UserStatusUpdateRequest request) {
        return userStatusService.updateByUserId(userId, request);
    }
}