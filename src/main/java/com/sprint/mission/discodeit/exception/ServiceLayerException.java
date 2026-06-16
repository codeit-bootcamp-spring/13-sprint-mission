package com.sprint.mission.discodeit.exception;

import lombok.Getter;
@Getter
public class ServiceLayerException extends RuntimeException{
    private final String type;
    public ServiceLayerException(String message, String type){
        super(message);
        this.type = type;
    }
}
