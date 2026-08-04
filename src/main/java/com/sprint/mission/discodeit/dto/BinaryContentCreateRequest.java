package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

// BinaryContent 생성 요청 DTO
// 메시지 첨부파일이나 사용자 프로필 이미지 같은 바이너리 데이터를 생성할 때 사용하는 클래스
@Getter
public class BinaryContentCreateRequest {

    // 업로드할 파일의 이름
    // 예: profile.png, document.pdf
    @NotBlank(message = "파일 이름은 필수입니다.")
    @Size(
            max = 255,
            message = "파일 이름은 255자 이하여야 합니다."
    )
    private String fileName;

    // 업로드할 파일의 MIME 타입
    // 예: image/png, image/jpeg, application/pdf
    @NotBlank(message = "파일 MIME 타입은 필수입니다.")
    @Size(
            max = 100,
            message = "파일 MIME 타입은 100자 이하여야 합니다."
    )
    private String contentType;

    // 업로드할 파일의 실제 바이너리 데이터
    // byte 배열 형태로 파일 내용을 저장함
    @NotEmpty(message = "파일 데이터는 비어 있을 수 없습니다.")
    private byte[] bytes;

    // BinaryContent 생성 요청 객체를 생성하는 생성자
    public BinaryContentCreateRequest(String fileName, String contentType, byte[] bytes) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
    }
}