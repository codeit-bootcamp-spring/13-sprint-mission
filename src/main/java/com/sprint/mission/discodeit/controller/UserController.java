package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserStatusRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    // 생성
    @RequestMapping(method = RequestMethod.POST)
    public UserResponse create(@RequestBody CreateUserRequest request) {
        return userService.create(request, Optional.empty());
    }

    // 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public UserResponse update(@PathVariable UUID id, @RequestBody UpdateUserRequest request) {
        return userService.update(
                id,
                request,
                Optional.empty()
        );
    }

    // 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

    // 전체 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    // 사용자 상태 업데이트
    @RequestMapping(method = RequestMethod.PUT)
    public UserStatusResponse update(@RequestBody UpdateUserStatusRequest request) {
        return userStatusService.updateByUserId(request);
    }


}
