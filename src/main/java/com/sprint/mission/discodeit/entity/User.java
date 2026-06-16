package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import static java.util.UUID.randomUUID;

@Getter
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private final UUID id;
    private UUID profileId;
    private final Instant createdAt;
    private Instant updatedAt;
    private String username, email, password; // 비밀번호 추가

    public User(String username, String email, String password, UUID profileId) {
        this.id= randomUUID();
        this.createdAt= Instant.now();
        this.updatedAt= Instant.now();
        this.username=username;
        this.email= email;
        this.password=password;
        this.profileId = profileId;
    }

    // updatedAt은 메소드 내부 수정이 발생했을 때만 현재 시간으로 수정하기 때문에 파라미터로 받지 않는다
    public void update(String newUserName, String newEmail, String newPassword, UUID newProfileId) {
        boolean anyValueUpdated=false; // updatedat은 실제 변경이 있을 때만 갱신되도록 구성
        if (newUserName != null && !newUserName.equals(this.username)){
            this.username=newUserName;
            anyValueUpdated=true;
        }
        if (newEmail != null && !newEmail.equals(this.email)){ // 기존 값과 다를 때 업데이트 되도록 해야 한다
            this.email=newEmail;
            anyValueUpdated=true;
        }
        if (newPassword != null && !newPassword.equals(this.password)){
            this.password=newPassword;
            anyValueUpdated=true;
        }
        if (newProfileId != null && !newProfileId.equals(this.profileId)){
            this.profileId=newProfileId;
            anyValueUpdated=true;
        }
        if (!anyValueUpdated){
            throw new IllegalArgumentException("변경사항이 없습니다!");
        }
        this.updatedAt=Instant.now();
    }

}
