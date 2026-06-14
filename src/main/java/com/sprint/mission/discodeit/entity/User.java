package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
// ##사용자 정보를 담는 클래스
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    //도메인 모델 정의 (6~
    //객체 타입선언
    private final UUID id; //고유번호
    private final Long createdAt; //만든시각
    private Instant updatedAt;  //바뀐시각
    private String username; //사용자이름
    private String email; //사용자 이메일
    private String password; //사용자 비밀번호
    private UUID profileId;

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화)
    // 생성자: 이름을 받아서 User 객체를 만듬.
    public User(String username, String email, String password,UUID profileId) { //만들 때 이름만 받음
        this.id = UUID.randomUUID(); //고유 번호 자동 생성
        this.createdAt = Instant.now().toEpochMilli();
        //this.createdAt = System.currentTimeMillis(); //지금 시각 (생성 시각 자동기록)
        this.username = username; //사용자 이름 저장
        this.email = email; //사용자 이메일 저장
        this.password = password; //사용자 비밀번호 저장
        this.profileId = profileId;
        this.updatedAt = Instant.now();
    }

    //이름을 수정하는 update 함수 (수정 시 update도 갱신)
    //update 함수 : 값을 바꿔중
    public void update(String newUsername, String newEmail, String newPassword) {
        boolean anyValueUpdated = false; // 실제 변경이 발생했는지 확인하기 위한 플래그
        this.updatedAt = Instant.ofEpochSecond(Instant.now().toEpochMilli());
        if (newUsername != null && !newUsername.equals(this.username)) {//username수정, 조건(1. null이 아니어야함. 2.기존값과 달라야함
            this.username = newUsername;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) { //email 수정, 조건 동일
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) { //password수정, 조건 동일
            this.password = newPassword;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) { //하나 이상의 값이 수정된 경우, 수정시간을 현재 시간으로 갱신
            this.updatedAt = Instant.ofEpochSecond(Instant.now().toEpochMilli()); //객체를 밀리초 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000123))
            // this.updatedAt = Instant.now().getEpochSecond(); // 객체를 초(Second) 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000))
        }
    }
}

