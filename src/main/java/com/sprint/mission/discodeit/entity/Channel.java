package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

// ##채팅방(채널) 정보를 담는 클래스
public class Channel implements Serializable {
    private final UUID id; //고유번호
    private final Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String name; //채널이름

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    //생성자: 이름을 받아서 Channel 객체를 만듬
    public Channel(String name) {
        this.id = UUID.randomUUID(); //고유 아이디 자동 생성
        this.createdAt = System.currentTimeMillis(); //생성시점 자동 기록
        this.updatedAt = this.createdAt; //처음엔 생성 시각과 같음
        this.name = name; //생성할 떄 이름만 받음
    }

    //각 필드를 반환하는 getter 함수
    //정보 꺼내는 함수도 있어요
    public UUID getId() {return id;}
    public long getCreatedAt() {return createdAt;}
    public long getUpdatedAt() {return updatedAt;}
    public String getName() {return name;}

    //이름을 수정하는 update 함수 (수정 시 update도 갱신)
    public void updateName(String newName) {
        this.name = newName;
        this.updatedAt = System.currentTimeMillis();
    }
}
