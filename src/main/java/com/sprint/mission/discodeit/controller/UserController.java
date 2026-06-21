package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    public User create(
            @RequestBody CreateUserRequest request
    ) {
        return userService.create(
                new User(
                        request.getUsername(),
                        request.getEmail()
                )
        );
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<User> findAll() {
        return userService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User find(
            @PathVariable UUID id
    ) {
        return userService.find(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public User update(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.update(
                id,
                request.getUsername(),
                request.getEmail()
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable UUID id
    ) {
        userService.delete(id);
    }
}