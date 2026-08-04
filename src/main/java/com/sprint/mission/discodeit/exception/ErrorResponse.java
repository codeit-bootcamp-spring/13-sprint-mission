package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 모든 예외 응답을 일관된 형식으로 반환하기 위한 DTO
 */
public record ErrorResponse(
        Instant timestamp,              // 예외 발생 시간
        String code,                    // ErrorCode 이름
        String message,                 // 사용자에게 전달할 예외 메시지
        Map<String, Object> details,     // 예외 관련 추가 정보
        String exceptionType,           // 발생한 예외 클래스 이름
        int status                      // HTTP 상태 코드
) {

    /**
     * details가 null이면 빈 Map으로 변환하고,
     * 외부에서 수정할 수 없도록 불변 Map으로 저장한다.
     */
    public ErrorResponse {
        details = details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(
                new LinkedHashMap<>(details)
        );
    }

    /**
     * DiscodeitException 계열의 커스텀 예외를
     * ErrorResponse로 변환한다.
     */
    public static ErrorResponse from(
            DiscodeitException exception,
            int status
    ) {
        return new ErrorResponse(
                exception.getTimestamp(),
                exception.getErrorCode().name(),
                exception.getMessage(),
                exception.getDetails(),
                exception.getClass().getSimpleName(),
                status
        );
    }

    /**
     * Spring 기본 예외처럼 DiscodeitException을 상속하지 않는 예외를
     * ErrorResponse로 변환할 때 사용한다.
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            Map<String, Object> details,
            Class<?> exceptionType,
            int status
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.name(),
                message == null ? errorCode.getMessage() : message,
                details,
                exceptionType.getSimpleName(),
                status
        );
    }
}