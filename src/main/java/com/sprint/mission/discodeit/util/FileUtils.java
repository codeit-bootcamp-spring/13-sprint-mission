package com.sprint.mission.discodeit.util;

import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.request.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Optional;

public final class FileUtils {

    private FileUtils() {
    }

    public static Optional<CreateBinaryContentCommand> toCommand(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    new CreateBinaryContentCommand(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes()
                    )
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "파일 변환에 실패했습니다: "
                            + file.getOriginalFilename(),
                    e
            );
        }
    }
}