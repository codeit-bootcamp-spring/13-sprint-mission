package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import javax.print.attribute.standard.*;
import java.io.*;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> create(
            @RequestPart("userCreateRequest") UserRequest.CreateUserRequest userCreateRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
       CreateBinaryContentRequest profileImageDto = null;
       if (profileImage != null && !profileImage.isEmpty()) {
           try {
               profileImageDto = new CreateBinaryContentRequest(
                       profileImage.getOriginalFilename(),
                       profileImage.getContentType(),
                       profileImage.getBytes()
               );
           } catch (IOException e) {
               throw new UncheckedIOException("프로필 이미지 파일을 읽는 중 오류가 발생했습니다.", e);
           }
       }

       UserResponse userResponse = userService.create(userCreateRequest, profileImageDto);

       return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
   }

    @PatchMapping(value = "/{userId}",
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse update(@PathVariable UUID userId,
                               @RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        UserRequest.UpdateUserRequest updateRequest = new UserRequest.UpdateUserRequest(
                username, email, password, profileImage
        );
        CreateBinaryContentRequest profileImageDto = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileImageDto = new CreateBinaryContentRequest(
                        profileImage.getOriginalFilename(),
                        profileImage.getContentType(),
                        profileImage.getBytes()
                );
            } catch (IOException e) {
                throw new UncheckedIOException("프로필 이미지 파일을 읽는 중 오류가 발생했습니다.", e);
            }
        }

        return userService.update(userId, updateRequest, profileImageDto);
    }

    @DeleteMapping(value = "/{userId}")
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PatchMapping(value = "/{userId}/status")
    public UserStatusResponse updateStatus(
            @PathVariable UUID userId
    ) {
        return userStatusService.updateByUserId(userId);
    }
}


