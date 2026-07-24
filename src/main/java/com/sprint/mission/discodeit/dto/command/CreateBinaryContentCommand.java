package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

public record CreateBinaryContentCommand(

        String fileName,
        String contentType,
        byte[] bytes
) {
        public CreateBinaryContentCommand from(
                CreateBinaryContentRequest request
        ) {
                return new CreateBinaryContentCommand(
                        request.fileName(),
                        request.contentType(),
                        request.bytes()
                );
        }


}
