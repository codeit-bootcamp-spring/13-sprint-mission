package com.sprint.mission.discodeit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 프로젝트에서 정의한 모든 커스텀 예외를 처리한다.
     *
     * HTTP 상태, 커스텀 에러 코드, 기본 메시지는
     * ErrorCode에 선언된 값을 사용한다.
     */
    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(
            DiscodeitException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error(
                    "서버 내부 커스텀 예외가 발생했습니다. code={}, status={}, details={}",
                    errorCode.getCode(),
                    errorCode.getHttpStatus().value(),
                    exception.getDetails(),
                    exception
            );
        } else {
            log.warn(
                    "커스텀 예외가 발생했습니다. code={}, status={}, details={}",
                    errorCode.getCode(),
                    errorCode.getHttpStatus().value(),
                    exception.getDetails()
            );
        }

        return buildResponse(exception);
    }

    /**
     * @Valid가 적용된 DTO의 검증 실패를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            details.putIfAbsent(
                    fieldError.getField(),
                    resolveValidationMessage(
                            fieldError.getDefaultMessage()
                    )
            );
        }

        for (ObjectError globalError
                : exception.getBindingResult().getGlobalErrors()) {

            details.putIfAbsent(
                    globalError.getObjectName(),
                    resolveValidationMessage(
                            globalError.getDefaultMessage()
                    )
            );
        }

        log.warn(
                "요청 값 검증에 실패했습니다. details={}",
                details
        );

        return buildResponse(
                ErrorCode.METHOD_ARGUMENT_NOT_VALID,
                null,
                details,
                exception
        );
    }

    /**
     * Validator를 직접 실행했을 때 발생한 검증 실패를 처리한다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation
                : exception.getConstraintViolations()) {

            String fieldName = extractFieldName(
                    violation.getPropertyPath().toString()
            );

            details.putIfAbsent(
                    fieldName,
                    violation.getMessage()
            );
        }

        log.warn(
                "요청 객체 검증에 실패했습니다. details={}",
                details
        );

        return buildResponse(
                ErrorCode.METHOD_ARGUMENT_NOT_VALID,
                null,
                details,
                exception
        );
    }

    /**
     * JSON 문법 오류 또는 요청 본문 역직렬화 실패를 처리한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        log.warn(
                "요청 본문을 읽을 수 없습니다. message={}",
                exception.getMessage()
        );

        return buildResponse(
                ErrorCode.MESSAGE_NOT_READABLE,
                null,
                Map.of(),
                exception
        );
    }

    /**
     * 요청 파라미터나 경로 변수의 타입 변환 실패를 처리한다.
     *
     * 예: UUID 자리에 abc를 전달한 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        details.put(
                "parameter",
                exception.getName()
        );

        if (exception.getValue() != null) {
            details.put(
                    "value",
                    exception.getValue()
            );
        }

        if (exception.getRequiredType() != null) {
            details.put(
                    "requiredType",
                    exception.getRequiredType().getSimpleName()
            );
        }

        log.warn(
                "요청 값의 타입이 올바르지 않습니다. parameter={}, value={}, requiredType={}",
                exception.getName(),
                exception.getValue(),
                exception.getRequiredType()
        );

        return buildResponse(
                ErrorCode.INVALID_REQUEST,
                "요청 값의 형식이 올바르지 않습니다.",
                details,
                exception
        );
    }

    /**
     * 필수 요청 파라미터가 누락된 경우를 처리한다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse>
    handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        details.put(
                "parameter",
                exception.getParameterName()
        );
        details.put(
                "type",
                exception.getParameterType()
        );

        log.warn(
                "필수 요청 파라미터가 누락되었습니다. parameter={}, type={}",
                exception.getParameterName(),
                exception.getParameterType()
        );

        return buildResponse(
                ErrorCode.INVALID_REQUEST,
                "필수 요청 파라미터가 누락되었습니다.",
                details,
                exception
        );
    }

    /**
     * 아직 커스텀 예외로 변환하지 않은
     * IllegalArgumentException을 처리한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        log.warn(
                "잘못된 요청이 발생했습니다. message={}",
                exception.getMessage()
        );

        return buildResponse(
                ErrorCode.INVALID_REQUEST,
                exception.getMessage(),
                Map.of(),
                exception
        );
    }

    /**
     * 존재하지 않는 정적 리소스 요청을 처리한다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleNoResourceFoundException(
            NoResourceFoundException exception
    ) {
        Map<String, Object> details = Map.of(
                "resourcePath",
                exception.getResourcePath()
        );

        log.debug(
                "정적 리소스를 찾을 수 없습니다. resourcePath={}",
                exception.getResourcePath()
        );

        return buildResponse(
                ErrorCode.RESOURCE_NOT_FOUND,
                null,
                details,
                exception
        );
    }

    /**
     * 위에서 처리되지 않은 모든 예외를 처리한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {
        log.error(
                "처리되지 않은 서버 예외가 발생했습니다.",
                exception
        );

        return buildResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                null,
                Map.of(),
                exception
        );
    }

    /**
     * 커스텀 예외를 ErrorResponse로 변환한다.
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            DiscodeitException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(exception));
    }

    /**
     * Spring 및 Java 기본 예외를 ErrorResponse로 변환한다.
     */
    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            Map<String, Object> details,
            Exception exception
    ) {
        ErrorResponse response = ErrorResponse.of(
                errorCode,
                message,
                details,
                exception.getClass()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * ConstraintViolation의 전체 경로에서
     * 마지막 필드 이름만 추출한다.
     *
     * 예:
     * create.request.username -> username
     */
    private String extractFieldName(
            String propertyPath
    ) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "validation";
        }

        int lastDotIndex = propertyPath.lastIndexOf('.');

        if (lastDotIndex < 0
                || lastDotIndex == propertyPath.length() - 1) {
            return propertyPath;
        }

        return propertyPath.substring(lastDotIndex + 1);
    }

    private String resolveValidationMessage(
            String message
    ) {
        if (message == null || message.isBlank()) {
            return "올바르지 않은 값입니다.";
        }

        return message;
    }
}