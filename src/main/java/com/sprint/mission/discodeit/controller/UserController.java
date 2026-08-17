package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @PostMapping
    public UserDto create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        log.debug("사용자 생성 요청: username={}, email={}",
                request.getUsername(),
                request.getEmail());

        UserDto userDto = userService.create(request);

        log.info("사용자 생성 완료");

        return userDto;
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.debug("사용자 목록 조회 요청");

        List<UserDto> users = userService.findAll();

        log.debug("사용자 목록 조회 완료: count={}", users.size());

        return users;
    }

    @GetMapping("/{id}")
    public UserDto find(
            @PathVariable UUID id
    ) {
        log.debug("사용자 단건 조회 요청: userId={}", id);

        return userService.find(id);
    }

    @PutMapping("/{id}")
    public UserDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.debug("사용자 수정 요청: userId={}", id);

        UserDto userDto = userService.update(id, request);

        log.info("사용자 수정 완료: userId={}", id);

        return userDto;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        log.debug("사용자 삭제 요청: userId={}", id);

        userService.delete(id);

        log.info("사용자 삭제 완료: userId={}", id);
    }
}