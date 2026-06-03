package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class User {
    //도메인 모델 정의 (6~
    //객체 타입선언
    private final UUID id; //고유번호
    private final Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String name; //사용자이름

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    public User(String name) { //만들 때 이름만 받음
        this.id = UUID.randomUUID(); //고유 번호 자동 생성
        this.createdAt = System.currentTimeMillis(); //지금 시각
        this.updatedAt = this.createdAt; //같은 값으로 시작
        this.name = name;
    }

    //Getter 함수 : name 값을 꺼내줌
    public UUID getId() {
        return id;
    }
    public long getCreatedAt() {return createdAt;}
    public long getUpdatedAt() {return updatedAt;}//이름 꺼내는 함수
    public String getName() {return name;}
    //update 함수 : name 값을 바꿔중
    public void updateName(String newName) { //이름 바꾸는 함수
        this.name = newName;
        this.updatedAt = System.currentTimeMillis(); //바꿀 떄마다 시각도 변경!
    }
}
