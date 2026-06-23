package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.input.*;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.basic.BasicUserStatusService;
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
public class UserController {

    private final BasicUserService bus;
    private final BasicUserStatusService buss;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll(){
        return ResponseEntity.ok(this.bus.getUserList());
    }

    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<User> create(
            @RequestPart("userCreateRequest") UserCreateRequest uci,
            @RequestPart(value = "thumbnail", required = false) MultipartFile tmb
    ) {
        Optional<BinaryContentCreate> bcc = Optional.ofNullable(tmb).flatMap(this::thumbnailResolver);
        User res = this.bus.create(uci, bcc);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @RequestMapping(value = "/{userId}",method = RequestMethod.DELETE)
    public ResponseEntity.BodyBuilder delete(
            @PathVariable UUID userId
    ){
        this.bus.delete(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<User> update(
            @PathVariable UUID userId,
            @RequestPart UserUpdateRequest uui,
            @RequestPart (value = "thumbnail", required = false) MultipartFile tmb
    ){
        Optional<BinaryContentCreate> bcc = Optional.ofNullable(tmb).flatMap(this::thumbnailResolver);
        User res = this.bus.update(userId, uui, bcc);
        return ResponseEntity.ok(res);
    }


    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatus> updateUserStatusByUserId(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest usur
    ){
        UserStatus res = buss.updateByUserId(userId, usur);
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
