package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

public record CreateBinaryContentRequest(

        @NotBlank(message = "파일 이름은 필수입니다.")
        String fileName,

        @NotBlank(message = "파일 타입은 필수입니다.")
        String contentType,

        @NotNull(message = "파일 데이터는 필수입니다.")
        @NotEmpty(message = "파일 데이터는 비어있을 수가 없습니다.")
        byte[] bytes
) {

    public CreateBinaryContentCommand toCommand() {
        return new CreateBinaryContentCommand(fileName, contentType, bytes);
    }
}
