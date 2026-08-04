package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
public class DiscodeitException extends RuntimeException {
    private final Instant timestamp = Instant.now();
    private final ExceptionCode code;
    private final Map<String,Object> details = new HashMap<>();

    public DiscodeitException(ExceptionCode code, String message, Map<String,Object> details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details.putAll(details);
    }

    public DiscodeitException(ExceptionCode code, String message, Map<String,Object> details) {
        super(message);
        this.code = code;
        this.details.putAll(details);
    }

    public DiscodeitException(ExceptionCode code, Map<String,Object> details) {
        super(code.defaultMessage());
        this.code = code;
        this.details.putAll(details);
    }

    public DiscodeitException(ExceptionCode code, String message){
        super(code.defaultMessage());
        this.code = code;
        this.details.put("detail",message);
    }

    public DiscodeitException(ExceptionCode code) {
        super(code.defaultMessage());
        this.code = code;
    }

    public HttpStatus status() {
        return code.status();
    }

    public ExceptionCode code(){
        return code;
    }

    public String defaultMessage(){
        return code.defaultMessage();
    }

    public Map<String,Object> details(){
        return details;
    }

    public Instant timestamp(){
        return timestamp;
    }

}
