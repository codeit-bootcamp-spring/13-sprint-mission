package com.sprint.mission.discodeit.exception;

import lombok.*;

import java.time.*;
import java.util.*;

@Getter
public class DiscodeitException extends RuntimeException {

    private final Instant timestamp;
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    // 추가 정보 없을 떄 사용
    public DiscodeitException(ErrorCode errorCode) {
        this(errorCode,Map.of());
    }

    // 사용자 ID같은 상세 정보 넣을 때 사용
    public DiscodeitException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode.getMessage());
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }
}
