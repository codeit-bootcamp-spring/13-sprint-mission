package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class Channel implements Serializable {

    private static final long serialVersionUID=1L;
    // 직렬화 및 역직렬화를 수행할 때 이 클래스의 버전을 의미

    private UUID id; // 객체 식별
    // UUID 범용 고유 식별자, 중복 되지 않는 유일한 값
    private final Long createdAt;
    private Long updatedAt; // 각각 객체의 생성, 수정 시간을 유닉스 타임스탬프로 나타냄

    private ChannelType type;
    private String name;


    // 생성자 호출
    public Channel(ChannelType type, String name, Long createdAt){

        this(randomUUID(), type, name, System.currentTimeMillis());


    }


    public Channel(UUID id, ChannelType type, String name, Long createdAt){
        this.id=id; // id 초기화
        this.type=normalizeChannelType(type);
        this.name=normalizeName(name);
        this.createdAt= createdAt; // 유낙스 타임스탬프 얻기
        this.updatedAt=createdAt;

    }






    // 필드 수정하는 update 함수 정의
    public void updateChannel(UUID id, ChannelType newType, String newName, Long updatedAt){

        this.id=id;
        this.type=normalizeChannelType(newType);
        this.name=normalizeName(newName);
        this.updatedAt=System.currentTimeMillis();

        System.out.println("사용자: "+id+"디스코드 채널\n"+newType+" > \n# "+newName+"\n수정: "+updatedAt);


        System.out.println("ESC...");

        System.out.println();

    }


    private String normalizeName(String newName){
        if(newName.isBlank()){
            return "1자에서 100자 사이여야 해요 채널 이름은 \"\"이(가) 될 수 없어요...";
        }
        return newName;
    }

    private ChannelType normalizeChannelType(ChannelType newType){
        if(newType!=ChannelType.PRIVATE&&newType!=ChannelType.PUBLIC) {
            System.out.println("채널은 PRIVATE과 PUBLIC만 사용 가능합니다...");
            return type;
        }
        return newType;

    }



    // 각 필드를 반환하는 Getter 함수를 정의
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }


}
