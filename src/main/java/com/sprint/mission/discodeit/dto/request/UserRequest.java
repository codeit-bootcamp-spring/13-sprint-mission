package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.*;
import org.springframework.web.multipart.*;

import java.time.*;
import java.util.*;

public record UserRequest(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String userName,
        String email,
        String password,
        UserStatus status
        ) {


    public record Create(
            String userName,
            String email,
            String password,
            MultipartFile profileImage
    ) {
    }

    public record FindById(UUID id){
    }


    public record FindAll(){
    }

    public record Update(
        UUID id,
        String userName,
        String email,
        String password
    ){
    }

    public record Delete(UUID id){
    }

    public static UserRequest from(Update updateDto, Instant originalCreatedAt, UserStatus originalStatus) {
        return new UserRequest(
                updateDto.id(),
                originalCreatedAt, // 기존 생성일 유지
                Instant.now(),     // 수정일은 지금 시간으로
                updateDto.userName(),
                updateDto.email(),
                updateDto.password(),
                originalStatus
        );
    }
}





