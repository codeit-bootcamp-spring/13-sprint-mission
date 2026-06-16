package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserAdvanceController {

    private final UserService userService;
    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/api/user/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserResponse> responses = userService.findAll();

        List<UserDto> userDtos = responses.stream()
                .map(user -> new UserDto(
                        user.id(),
                        user.createdAt(),
                        user.updatedAt(),
                        user.username(),
                        user.email(),
                        user.profileId(),
                        user.isOnline()
                ))
                .toList();

        return ResponseEntity.ok(userDtos);
    }

    @RequestMapping(value = "/api/binaryContent/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findBinaryContent(@RequestParam(name = "binaryContentId") UUID binaryContentId) {
        BinaryContent binaryContent = binaryContentService.findEntityById(binaryContentId);

        return ResponseEntity.ok(binaryContent);
    }

}
