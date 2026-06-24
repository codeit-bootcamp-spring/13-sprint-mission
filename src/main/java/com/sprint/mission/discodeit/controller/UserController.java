package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    // 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public Object createUser(
            @RequestBody UserCreateRequest request
    ) {

        return userService.create(
                request,
                Optional.empty()
        );
    }

    // 모든 사용자 조회
    @RequestMapping(method = RequestMethod.GET)
    public Object getAllUsers() {

        return userService.findAll();
    }

    // 사용자 정보 수정
    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.PUT
    )
    public Object updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request
    ) {

        return userService.update(
                userId,
                request,
                Optional.empty()
        );
    }

    // 사용자 삭제
    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.DELETE
    )
    public void deleteUser(
            @PathVariable UUID userId
    ) {

        userService.delete(userId);
    }

    // 온라인 상태 업데이트
    @RequestMapping(
            value = "/status/{userId}",
            method = RequestMethod.PUT
    )
    public Object updateOnlineStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest request
    ) {

        return userStatusService.updateByUserId(
                userId,
                request
        );
    }
}