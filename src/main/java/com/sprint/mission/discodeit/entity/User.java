package com.sprint.mission.discodeit.entity;

//너무 내부 로직에 치우친 공부-> 메서드 호출 방향, 받은 값의 출처, 반환 값받을 누가 받는지 등을 더 참고.

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
@Getter
public class User implements Serializable {//
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String userName;
    private String password;
    private String email;
    private UUID profileId;


    public User(String username, String password, String email) {//유저가 입력한 문자열을 받아서 {}를 실행
        this.id = UUID.randomUUID(); //randomUUID();이 만든 무작위 번호 할당
        this.createdAt = Instant.now().getEpochSecond(); //String username를 입력한 유닉스 타임 할당
        this.updatedAt = this.createdAt; //수정 시간을 유닉스타임과 같게 함.
        this.userName = username;//(String username)로 받은 정보를 할당
        this.password = password;
        this.email = email;
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public void update(String newUsername, String newEmail, String newPassword) {
        boolean anyValueUpdated = false;
        if(newUsername != null && !newUsername.equals(this.userName)) {
            this.userName = newUsername;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)){
            this.password = newPassword;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now().getEpochSecond();
        }

    }
}