package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class UserMultipartMapper {

    public UserCreateRequest toCreateRequest(
            UserCreateRequest request,
            MultipartFile profile
    ) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("사용자 생성 요청은 비어 있을 수 없습니다.");
        }

        BinaryContentCreateRequest profileImage =
                toBinaryContentCreateRequest(profile);

        return new UserCreateRequest(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                profileImage
        );
    }

    public UserUpdateRequest toUpdateRequest(
            UUID id,
            UserUpdateRequest request,
            MultipartFile profile
    ) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("수정할 사용자 id는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("사용자 수정 요청은 비어 있을 수 없습니다.");
        }

        BinaryContentCreateRequest profileImage =
                toBinaryContentCreateRequest(profile);

        return new UserUpdateRequest(
                id,
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getProfileId(),
                profileImage
        );
    }

    private BinaryContentCreateRequest toBinaryContentCreateRequest(
            MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "unknown";
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return new BinaryContentCreateRequest(
                fileName,
                contentType,
                file.getBytes()
        );
    }
}