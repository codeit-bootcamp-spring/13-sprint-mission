package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String message;
    private UUID authorId;
    private UUID channelId;

    //객체 생성할때 Message mgs = new Message("공부 중입니다!",userA.getId());
    //이렇게 객체 생성시 객체 뒤에 있는 ()안에 인자를
    // 생성자의 뒤에있는()매개변수와 순서를 동일하게 하여야만 정싱적으로 작동한다.
    //즉, 생성자(String message, UUID userId)는 문자열, UUID타입의 유저id 순이면,
    //반드시 객체("공부 중입니다!",userA.getId());로 문자열, UUID타입의 유저id를 답는 변수
    //서순으로 작성을 해야만 정상적인 작동을 한다/ 타입 서순이 다르면 컴파일 에러.
    //즉 생성자의 매개변수와 생성자의 메서드 내부 서순은 무관하다,(단, 서순을 맞게 배치하는게 가독성 좋음)
    public Message(String message, UUID authorId,  UUID channelId) {
        this.id = UUID.randomUUID();//쓴 글에 고유 id
        this.createdAt = System.currentTimeMillis();//글쓴 시간
        this.updatedAt = this.createdAt;//최초 업데이트 시간
        this.message = message;//메세지 내용이
        this.authorId = authorId;//글쓴 사람
        this.channelId = channelId;//작성한 채널
    }


    public UUID getId() {return id;}
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}
    public String getMessage() {return message;}
    public UUID getAuthorId() {return authorId;}
    public UUID getChannelId() {return channelId;}


    public void updateMessage(String newMessage) {
        this.message = newMessage;
        this.updatedAt = System.currentTimeMillis();
    }

}
