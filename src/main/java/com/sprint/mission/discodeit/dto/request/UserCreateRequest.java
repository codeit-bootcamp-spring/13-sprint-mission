package com.sprint.mission.discodeit.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UserCreateRequest(
        String name,
        String email,
        String password,
        String profileImagePath

) {

}
