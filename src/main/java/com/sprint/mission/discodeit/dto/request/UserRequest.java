package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.web.multipart.*;

import java.time.*;
import java.util.*;

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
            UUID id,
            String username,
            String email,
            String password,
            MultipartFile profileImage
    ) {
    }

}





