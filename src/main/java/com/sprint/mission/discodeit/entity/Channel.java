package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;
//멘토님의 조언을 적용해서 메서드이 흐름 위주로 그려보자.


public class Channel implements Serializable {
    //메인에서 Channel channel = new channel();로 객체를 생성한다.
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String channelName;
    private String description;
    //private ChannelType typ;
    //생성자가 호출되며 값이 할당된다.
    //channel객체 생성 - > 생성자 호출 *변수 선언에는 권한이 안 옴.


    public Channel(String chName, String description) {
        //호출자 main클레스 Channel channel = new Channel("채널이름","채널소개")로 생성자 호출
        //
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.channelName = chName;
        this.description = description;
        //this.type = ChannelType;(나중에 클레스 만들면 매개변수에 추가 해야함.)
        //각 필드 변수에 값을 할당 ->
        //필드 변수가 채워진 Channel객체를 참조 변수 channel에 반환 -> 메인으로 권한 복귀
    }


    public UUID getId() {
        //호출1 : 메인의 channeservice.read(channel.getId())에서 호출
        //호출 2: JCFChannelService의 내부 for문에서 id 비교할 떄 호출
        //반환 값 향한 곳:JCFChannelService의 read(UUID id)내부 로직
        //반환 값의 활용: id 값 반환 후 호출한 곳으로 복귀
        return id;
    }

    public Long getCreatedAt() {
        //main에서 정보를 조회할 때 호출
        //createdAt값 반환 -> 호출한 곳으로 복귀.
        return createdAt;
    }

    public Long getUpdatedAt() {
        //main에서 정보 조회할 떄 호출
        //updatedAt값 반환 -> 호출한 곳으로 복귀
        return updatedAt;
    }

    public String getChannelName() {
        //main에서 정보 조회할 때 호출
        //channelName값 반환 -> 호출한 곳으로 복귀
        return channelName;
    }

    public String getDescription() {
        //main에서 정보 조회할 떄 호출
        //description값 반환 -> 호출한 곳으로 복귀
        return description;
    }
    //public ChannelType getType() {return type};


    public void updateChName(String newChannelName) {
        //값을 Channel.update에서 호출
        //값을 할당 받고 팔드 재 할당
        //실행 권한 Channel.update()로 복귀
        this.channelName = newChannelName;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateDescription(String newDescription) {
        //Channle.update에서 호출
        //할당 받은 값을 필드에 재할당
        //실행 권한이 Channel.update()로 복귀
        this.description = newDescription;
        this.updatedAt = System.currentTimeMillis();
    }
    //public void updateType(ChannelType newType)


    public void update(String chName, String chDescription) {
        //JCFChannelService.update()에서
        // foundChannel.update(chName, description);이떄 호출
        //받은 값을 updateChName(), updateDescription()에 넘겨 필드 수정
        //Channel.updatte()가 끝나면 호출한 JCFChannelService.update()로 살행권 복귀
        this.updateChName(chName);
        this.updateDescription(chDescription);

    }
}
