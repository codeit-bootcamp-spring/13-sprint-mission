package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        // 에러 발생 시각
        Instant timestamp,

        // HTTP 상태 코드
        int status,

        // 발생한 예외 클래스 이름
        String exceptionType,

        // ErrorCode Enum 이름
        String code,

        // 클라이언트에게 보여줄 메시지
        String message,

        // 에러 상황의 추가 정보
        Map<String, Object> details
) {

    public static ErrorResponse of(DiscodeitException e) {
        return new ErrorResponse(
                Instant.now(),
                e.getErrorCode().getStatus().value(),  // HttpStatus → int 변환
                e.getClass().getSimpleName(),          // 예외 클래스 이름
                e.getErrorCode().name(),               // ErrorCode Enum 이름
                e.getErrorCode().getMessage(),
                e.getDetails()
        );
    }
}