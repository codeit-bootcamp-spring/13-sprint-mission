package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;

public enum ExceptionCode {
    FILE_SYSTEM_ERROR("F001",INTERNAL_SERVER_ERROR,"File System Error"),
    REQUEST_VALUE_ERROR("C003",BAD_REQUEST,"Request value error"),
    INVALID_AUTHENTICATION("C002",FORBIDDEN,"Invalid Authentication"),
    RESOURCE_NOT_FOUNDED("C001",NOT_FOUND,"Resource not founded"),
    SERVER_ERROR("C999",INTERNAL_SERVER_ERROR,"Uncacheable Error");


    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ExceptionCode(String code, HttpStatus status, String defaultMessage){
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code(){
        return code;
    }

    public HttpStatus status(){
        return status;
    }

    public String defaultMessage(){
        return defaultMessage;
    }
}
