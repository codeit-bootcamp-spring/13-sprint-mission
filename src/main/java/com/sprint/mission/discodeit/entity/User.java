package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

// ##사용자 정보를 담는 클래스
public class User implements Serializable {
    //도메인 모델 정의 (6~
    //객체 타입선언
    private final UUID id; //고유번호
    private final Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String name; //사용자이름
    private String email;
    private String password;

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화)
    // 생성자: 이름을 받아서 User 객체를 만듬.
    public User(String name, String email, String password) { //만들 때 이름만 받음
        this.id = UUID.randomUUID(); //고유 번호 자동 생성
        this.createdAt = System.currentTimeMillis(); //지금 시각 (생성 시각 자동기록)
        this.name = name; //생성 할때 이름을 받음
        this.email = email;
        this.password = password;
    }

    //각 필드에 받환하는 getter 함수
    //Getter 함수 : name 값을 꺼내줌
    public UUID getId() {
        return id;
    }
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}//이름 꺼내는 함수
    public String getName() {return name;}
    public String getEmail() {return email;}
    public String getPassword() {return password;}

    //이름을 수정하는 update 함수 (수정 시 update도 갱신)
    //update 함수 : name 값을 바꿔중
    public void updateUser(String newUsername, String newEmail, String newPassword) {
        boolean anyValueUpdated = false;
        if (newUsername != null && !newUsername.equals(this.name)) {
            this.name = newUsername;
            anyValueUpdated = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            anyValueUpdated = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now().getEpochSecond();
        }
    }
}
