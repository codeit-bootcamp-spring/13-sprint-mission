package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.*;

@Schema(description = "유저 정보 요청")
public record UserRequest(

        @NotNull(message = "유저 이름은 필수입니다.")
        String userName,
        @NotNull(message = "이메일은 필수입니다.")
        String email,
        @NotNull(message = "비밀번호는 필수입니다.")
        String password,
        MultipartFile profileImage
        ) {

    public record CreateUserRequest(
            String username,
            String email,
            String password,
            MultipartFile profileImage
    ) {
    }

    public record ProfileImageRequest(
            String fileName,
            String contentType,
            byte[] data
    ) {
    }

    public record UpdateUserRequest(
            String username,
            String email,
            String password,
            MultipartFile profileImage
    ) {
    }

}





