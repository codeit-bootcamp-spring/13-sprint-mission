package com.sprint.mission.discodeit.util;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.ContentType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class FileUtils {
    public static Optional<BinaryContentCreateRequest> toRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        ContentType contentType = Arrays.stream(ContentType.values())
                .filter(ct-> ct.getValue().equals(file.getContentType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 파일 형식입니다."));
        try {
            return Optional.of(new BinaryContentCreateRequest(
                    file.getOriginalFilename(), file.getSize(), contentType, file.getBytes()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
