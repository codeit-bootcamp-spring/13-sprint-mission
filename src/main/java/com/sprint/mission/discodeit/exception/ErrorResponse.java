package com.sprint.mission.discodeit.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String exceptionType;
    private final Map<String, Object> details;

    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.exceptionType = builder.exceptionType;
        this.details = builder.details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public static class Builder {

        private LocalDateTime timestamp;
        private int status;
        private String code;
        private String message;
        private String exceptionType;
        private Map<String, Object> details;

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder exceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}