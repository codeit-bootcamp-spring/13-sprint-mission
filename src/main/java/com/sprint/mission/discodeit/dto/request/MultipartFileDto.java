package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.BinaryContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MultipartFileDto(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long size,
        // 사용자 측 데이터 저장.
        // 데이터가 커지면, 임시파일 리스트로 저장.
        // 이후 비동기로 파일을 읽어서 저장소에 이동.
        // 비동기 시에, 요청이 닫히면 인풋 스트림이 OS 에 의해 정리될 수 있기 때문.
        @NotNull byte[] content
) {
    public BinaryContent toBinaryContent(){
        return new BinaryContent(
                filename,
                contentType,
                size
        );
    }
}
