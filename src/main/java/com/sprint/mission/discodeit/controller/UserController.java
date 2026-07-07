package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.UserControllerDoc;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/api/users"})
public class UserController implements UserControllerDoc {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll(){
        return ResponseEntity.ok(this.userService.getUserList());
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<UserDto> create(
            @RequestPart("userCreateRequest") UserCreateRequest uci,
            @RequestPart(value = "profile", required = false) MultipartFile tmb
    ) {
        Optional<BinaryContentCreate> bcc = Optional.ofNullable(tmb).flatMap(this::thumbnailResolver);
        UserDto res = this.userService.create(uci, bcc);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{userId}",method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(
            @PathVariable UUID userId
    ){
        this.userService.delete(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<UserDto> update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") UserUpdateRequest uui,
            @RequestPart (value = "profile", required = false) MultipartFile tmb
    ){
        Optional<BinaryContentCreate> bcc = Optional.ofNullable(tmb).flatMap(this::thumbnailResolver);
        UserDto res = this.userService.update(userId, uui, bcc);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusDto> updateUserStatusByUserId(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest usur
    ){
        UserStatusDto res = userStatusService.updateByUserId(userId, usur);
        return ResponseEntity.ok(res);
    }



    private Optional<BinaryContentCreate> thumbnailResolver(MultipartFile tmb) {
        if (tmb.isEmpty()) return Optional.empty();
        try{
            String filename = tmb.getOriginalFilename();
            String contentType = tmb.getContentType();
            Long fileSize = tmb.getSize();
            byte[] content = tmb.getBytes();

            BinaryContentCreate bc = new BinaryContentCreate(
                    filename,
                    contentType,
                    fileSize,
                    content
            );

            return Optional.of(bc);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }


}
