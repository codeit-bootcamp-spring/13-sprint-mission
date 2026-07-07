package com.sprint.mission.discodeit.util;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public class FileUtils {
    public static Optional<BinaryContentCreateRequest> toRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BinaryContentCreateRequest(
                    file.getOriginalFilename(), file.getSize(), file.getContentType(), file.getBytes()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
