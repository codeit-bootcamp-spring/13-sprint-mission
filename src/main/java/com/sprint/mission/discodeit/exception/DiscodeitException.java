package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

import static com.sprint.mission.discodeit.exception.ExceptionCode.*;

public class DiscodeitException extends RuntimeException {
    private final Instant timestamp = Instant.now();
    private final ExceptionCode code;
    private final String message;

    public DiscodeitException(ExceptionCode code, String format, Object... args ){
        super(code.defaultMessage());
        this.code = code;
        this.message = String.format(format, args);
    }

    public DiscodeitException(ExceptionCode code, String message){
        super(code.defaultMessage());
        this.code = code;
        this.message = message;
    }

    public DiscodeitException(ExceptionCode code) {
        super(code.defaultMessage());
        this.code = code;
        this.message = code.defaultMessage();
    }

    public DiscodeitException(){
        super(SERVER_ERROR.defaultMessage());
        this.code = SERVER_ERROR;
        this.message = SERVER_ERROR.defaultMessage();
    }

    public HttpStatus status() {
        return code.status();
    }

    public ExceptionCode code(){
        return code;
    }

    public String message() { return message; }

    public Instant timestamp(){
        return timestamp;
    }

}
