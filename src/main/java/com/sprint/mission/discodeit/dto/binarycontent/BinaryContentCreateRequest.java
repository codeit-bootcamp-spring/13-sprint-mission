package com.sprint.mission.discodeit.dto.binarycontent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BinaryContentCreateRequest { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언
    private String fileName;
    private String contentType;
    private Long fileSize;
    private byte[] bytes;
}
