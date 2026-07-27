package com.sprint.mission.discodeit.dto.request;

public record LoginRequest (
        String username,
        String password
){
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
