package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 1. 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public UserResponse createUser(@RequestBody UserRequest userRequest) {
        return userService.create(userRequest);
    }

    // 2. 특정 사용자 (단건) 조회
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.findById(id)
                .orElseThrow(() -> new DiscodeitException.UserNotFoundException("해당 사용자를 찾을 수 없습니다."));
    }

    // 3. 전체 사용자 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<UserResponse> getAllUsers() {
        return userService.findAll();
    }

    // 4. 사용자 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public UserResponse updateUser(@PathVariable UUID id, @RequestBody UserRequest userRequest) {
        return userService.update(id, userRequest);
    }

    // 5. 사용자 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }
}

// [ ]  지금까지 구현한 서비스 로직을 활용해 웹 API를 구현하세요.
//      이때 @RequestMapping만 사용해 구현해보세요.