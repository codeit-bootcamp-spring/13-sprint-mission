package com.sprint.mission.discodeit.service;

public class JCFException extends RuntimeException {
    public JCFException(String message) {
        super(message);
    }

    public static void throwRuntimeError(String message){
        throw new JCFException(message);
    }
}
