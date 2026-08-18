package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 모든 예외 응답을 일관된 형식으로 반환하기 위한 DTO
 */
public record ErrorResponse(

        // 예외 발생 시간
        Instant timestamp,

        // HTTP 상태 코드
        int status,

        // HTTP 상태 이름
        String error,

        // 애플리케이션 커스텀 에러 코드
        String code,

        // 사용자에게 전달할 예외 메시지
        String message,

        // 예외 관련 추가 정보
        Map<String, Object> details,

        // 발생한 예외 클래스 이름
        String exceptionType
) {

    /**
     * details가 null이면 빈 Map으로 변환하고,
     * 외부에서 수정할 수 없도록 불변 Map으로 저장한다.
     */
    public ErrorResponse {
        timestamp = timestamp == null
                ? Instant.now()
                : timestamp;

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
            DiscodeitException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        return new ErrorResponse(
                exception.getTimestamp(),
                errorCode.getHttpStatus().value(),
                errorCode.getHttpStatus().getReasonPhrase(),
                errorCode.getCode(),
                resolveMessage(
                        exception.getMessage(),
                        errorCode
                ),
                exception.getDetails(),
                exception.getClass().getSimpleName()
        );
    }

    /**
     * 필요한 경우 HTTP 상태 코드를 직접 전달하는 호환용 메서드
     *
     * 가능하면 ErrorCode에 선언된 HttpStatus를 사용하는
     * from(DiscodeitException) 사용을 권장한다.
     */
    public static ErrorResponse from(
            DiscodeitException exception,
            int status
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        return new ErrorResponse(
                exception.getTimestamp(),
                status,
                errorCode.getHttpStatus().getReasonPhrase(),
                errorCode.getCode(),
                resolveMessage(
                        exception.getMessage(),
                        errorCode
                ),
                exception.getDetails(),
                exception.getClass().getSimpleName()
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
            Class<?> exceptionType
    ) {
        validateErrorCode(errorCode);

        return new ErrorResponse(
                Instant.now(),
                errorCode.getHttpStatus().value(),
                errorCode.getHttpStatus().getReasonPhrase(),
                errorCode.getCode(),
                resolveMessage(message, errorCode),
                details,
                resolveExceptionType(exceptionType)
        );
    }

    /**
     * 별도의 메시지나 상세 정보 없이
     * ErrorCode 정보만으로 응답을 생성한다.
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            Class<?> exceptionType
    ) {
        return of(
                errorCode,
                null,
                Collections.emptyMap(),
                exceptionType
        );
    }

    /**
     * 메시지만 별도로 지정하여 응답을 생성한다.
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            Class<?> exceptionType
    ) {
        return of(
                errorCode,
                message,
                Collections.emptyMap(),
                exceptionType
        );
    }

    /**
     * 상세 정보만 별도로 지정하여 응답을 생성한다.
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            Map<String, Object> details,
            Class<?> exceptionType
    ) {
        return of(
                errorCode,
                null,
                details,
                exceptionType
        );
    }

    /**
     * 기존 GlobalExceptionHandler 코드와의 호환을 위해
     * 상태 코드를 직접 전달받는 메서드
     */
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            Map<String, Object> details,
            Class<?> exceptionType,
            int status
    ) {
        validateErrorCode(errorCode);

        return new ErrorResponse(
                Instant.now(),
                status,
                errorCode.getHttpStatus().getReasonPhrase(),
                errorCode.getCode(),
                resolveMessage(message, errorCode),
                details,
                resolveExceptionType(exceptionType)
        );
    }

    private static String resolveMessage(
            String message,
            ErrorCode errorCode
    ) {
        if (message == null || message.isBlank()) {
            return errorCode.getMessage();
        }

        return message;
    }

    private static String resolveExceptionType(
            Class<?> exceptionType
    ) {
        return exceptionType == null
                ? "UnknownException"
                : exceptionType.getSimpleName();
    }

    private static void validateErrorCode(
            ErrorCode errorCode
    ) {
        if (errorCode == null) {
            throw new IllegalArgumentException(
                    "ErrorCode는 null일 수 없습니다."
            );
        }
    }
}