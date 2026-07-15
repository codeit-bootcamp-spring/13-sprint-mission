package com.sprint.mission.discodeit.dto;

import lombok.Getter;

import java.util.Base64;
import java.util.UUID;

@Getter
public class BinaryContentDownloadResponse {

    private UUID id;

    private String fileName;

    private String contentType;

    /*
     * 기존 호환용 필드
     *
     * byte[]는 Jackson이 JSON으로 응답할 때 보통 Base64 문자열로 직렬화한다.
     * 기존 코드에서 bytes를 사용하고 있을 수 있으므로 유지한다.
     */
    private byte[] bytes;

    /*
     * 프론트 호환용 Base64 문자열
     *
     * 일부 프론트에서는 byte[]보다 Base64 문자열을 직접 기대할 수 있어서 추가한다.
     */
    private String base64Bytes;

    /*
     * 프론트에서 바로 이미지 src로 사용할 수 있는 형태
     *
     * 예:
     * data:image/png;base64,iVBORw0KGgoAAA...
     */
    private String dataUrl;

    private Long size;

    public BinaryContentDownloadResponse(
            UUID id,
            String fileName,
            String contentType,
            byte[] bytes,
            Long size
    ) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
        this.size = size;

        if (bytes != null && contentType != null) {
            this.base64Bytes = Base64.getEncoder().encodeToString(bytes);
            this.dataUrl = "data:" + contentType + ";base64," + this.base64Bytes;
        }
    }
}
