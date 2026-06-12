package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UserCreateRequest(

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String name,

        @NotBlank(message = "email은 비워둘 수 없습니다.")
        String email,

        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password,

        String profileImagePath

) {

}
