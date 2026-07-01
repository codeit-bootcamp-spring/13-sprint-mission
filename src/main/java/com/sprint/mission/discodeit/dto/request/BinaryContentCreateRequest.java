package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//바이너리 파일(첨부파일) 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryContentCreateRequest {
    private String fileName; //업로드할 파일
    private String contentType; //파일의 MIME 타입(content-Type)이고 파일 종류를 식별하기 위해 사용됨.
    private byte[] bytes; //실제 파일 데이터(Byte 배열). 파일의 내용을 바이트 단위로 저장함.
}
