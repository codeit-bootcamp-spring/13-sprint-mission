package com.sprint.mission.discodeit.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class DiscodeitException extends RuntimeException {

    private final Instant timestamp;
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    /**
     * ErrorCode의 기본 메시지만 사용하는 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode
    ) {
        this(
                errorCode,
                null,
                Collections.emptyMap(),
                null
        );
    }

    /**
     * ErrorCode와 상세 정보만 전달하는 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode,
            Map<String, Object> details
    ) {
        this(
                errorCode,
                null,
                details,
                null
        );
    }

    /**
     * ErrorCode와 사용자 지정 메시지를 전달하는 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode,
            String message
    ) {
        this(
                errorCode,
                message,
                Collections.emptyMap(),
                null
        );
    }

    /**
     * ErrorCode, 사용자 지정 메시지, 상세 정보를 전달하는 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode,
            String message,
            Map<String, Object> details
    ) {
        this(
                errorCode,
                message,
                details,
                null
        );
    }

    /**
     * ErrorCode, 상세 정보, 원인 예외를 전달하는 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode,
            Map<String, Object> details,
            Throwable cause
    ) {
        this(
                errorCode,
                null,
                details,
                cause
        );
    }

    /**
     * 모든 정보를 전달하는 최종 생성자
     */
    public DiscodeitException(
            ErrorCode errorCode,
            String message,
            Map<String, Object> details,
            Throwable cause
    ) {
        super(
                resolveMessage(errorCode, message),
                cause
        );

        this.timestamp = Instant.now();
        this.errorCode = requireErrorCode(errorCode);
        this.details = toImmutableDetails(details);
    }

    /**
     * ErrorCode가 null인지 검증한다.
     */
    private static ErrorCode requireErrorCode(
            ErrorCode errorCode
    ) {
        if (errorCode == null) {
            throw new IllegalArgumentException(
                    "ErrorCode는 null일 수 없습니다."
            );
        }

        return errorCode;
    }

    /**
     * 사용자 지정 메시지가 없으면 ErrorCode의 기본 메시지를 사용한다.
     */
    private static String resolveMessage(
            ErrorCode errorCode,
            String message
    ) {
        ErrorCode validatedErrorCode =
                requireErrorCode(errorCode);

        if (message == null || message.isBlank()) {
            return validatedErrorCode.getMessage();
        }

        return message;
    }

    /**
     * details를 null이 아닌 불변 Map으로 변환한다.
     */
    private static Map<String, Object> toImmutableDetails(
            Map<String, Object> details
    ) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(details)
        );
    }
}