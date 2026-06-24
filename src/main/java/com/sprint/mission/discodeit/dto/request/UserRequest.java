package com.sprint.mission.discodeit.dto.request;

import org.springframework.web.multipart.*;


public record UserRequest(
        String userName,
        String email,
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





