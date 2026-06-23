package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
// ##채팅방(채널) 정보를 담는 클래스
public class Channel implements Serializable {
    private static final long SERIAL_VERSION_UID = 1L; //직렬화 버전 식별자
    private final UUID id; //고유번호
    private Instant createdAt; //만든시각
    private Instant updatedAt;  //바뀐시각
    private String name; //채널이름
    private String description; //채널 설명
    private ChannelType type; //채널 타입

    // 생성자(id/createdAt 생성자에서 초기화, id/createdAt/updateAt 제외한 필드는 생성자의 파라미터를 통해 초기화
    //생성자: 이름을 받아서 Channel 객체를 만듬
    public Channel(ChannelType type, String name, String description) {
        this.id = UUID.randomUUID(); //고유 아이디 자동 생성
        //this.createdAt = System.currentTimeMillis(); //생성시점 자동 기록
        this.createdAt = Instant.now(); //채널 생성 시각 저장
        this.type = type; //채널 타입저장
        this.name = name; //채널 이름 저장
        this.description = description; //채널 설명저장
    }

    //채널 정보 수정 메서드(채널 이름과 설명을 수정함)
    public void update(String newName, String newDescription) {
        boolean anyValueUpdated = false; //실제 변경 발생 여부를 확인하기 위한 변수
        this.updatedAt = Instant.now(); //수정 요청 시 현재 시각 저장
        if (newName != null && !newName.equals(this.name)) { //새로운 이름이 존재하고 기존 이름과 다를 경우 채널이름수정
            this.name = newName;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) { //새로운 설명이 존재하고 기존 설명과 다를 경우 채널 설명 수정
            this.description = newDescription;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) { //실제 값이 변경된 경우에만 수정 시간 갱신
            this.updatedAt = Instant.ofEpochSecond(Instant.now().toEpochMilli());//객체를 밀리초 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000123))
            // this.updatedAt = Instant.now().getEpochSecond(); // 객체를 초(Second) 단위로 변환 (예 1970-01-01 00:00:00 UTC 의미함 (1749636000))
        }
    }
}

