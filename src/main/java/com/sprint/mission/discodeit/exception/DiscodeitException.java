package com.sprint.mission.discodeit.exception;

public abstract class DiscodeitException extends RuntimeException{
    public DiscodeitException(String message){
        super(message);
    }
    public abstract String getErrorType();
    public abstract String getErrorDescription();

    public static class UserNotFoundException extends DiscodeitException{
        public UserNotFoundException(String message){
            super(message);
        }
        @Override
        public String getErrorType() {
            return "USER_ERROR";
        }
        @Override
        public String getErrorDescription() {
            return "해당 사용자를 찾을 수 없습니다.";
        }
    }

    public static class ChannelNotFoundException extends DiscodeitException{
        public ChannelNotFoundException(String message){
            super(message);
        }
        @Override
        public String getErrorType() {
            return "CHANNEL_ERROR";
        }
        @Override
        public String getErrorDescription() {
            return "해당 채널을 찾을 수 없습니다";
        }
    }

    public static class MessageNotFoundException extends DiscodeitException{
        public MessageNotFoundException(String message){
            super(message);
        }
        @Override
        public String getErrorType() {
            return "MESSAGE_ERROR";
        }
        @Override
        public String getErrorDescription() {
            return "해당 메세지를 찾을 수 없습니다";
        }
    }

    public static class FileNotFoundException extends DiscodeitException{
        public FileNotFoundException(String message){
            super(message);
        }
        @Override
        public String getErrorType() {
            return "FILE_ERROR";
        }
        @Override
        public String getErrorDescription() {
            return "해당 파일을 찾을 수 없습니다.";
        }
    }
}
