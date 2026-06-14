package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
// ##채팅방(채널) 정보를 담는 클래스
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id; //고유번호
    private Long createdAt; //만든시각
    private Long updatedAt;  //바뀐시각
    private String name; //채널이름
    private String description; //채널 설명
    private ChannelType type; //채널 타입

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    //생성자: 이름을 받아서 Channel 객체를 만듬
    public Channel(ChannelType type, String name, String description) {
        this.id = UUID.randomUUID(); //고유 아이디 자동 생성
        //this.createdAt = System.currentTimeMillis(); //생성시점 자동 기록
        this.createdAt = Instant.now().toEpochMilli();
        this.type = type; //채널 타입저장
        this.name = name; //채널 이름 저장
        this.description = description; //채널 설명저장
    }

    //이름을 수정하는 update 함수 (수정 시 update도 갱신)
    public void update(String newName, String newDescription) {
        boolean anyValueUpdated = false;
        this.updatedAt = Instant.now().toEpochMilli();
        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now().toEpochMilli();//객체를 밀리초 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000123))
            // this.updatedAt = Instant.now().getEpochSecond(); // 객체를 초(Second) 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000))
        }
    }
}

