package com.sprint.mission.discodeit.util;


import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public class FileUtils {
    public static Optional<BinaryContentCreateCommand> toCommand(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        try {
            BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                    file.getOriginalFilename(), file.getSize(), file.getContentType(), file.getBytes()
            );
            return Optional.of(request.toCommand());
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }
}
