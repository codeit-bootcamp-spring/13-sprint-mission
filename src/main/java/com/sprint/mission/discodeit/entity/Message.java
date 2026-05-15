package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class Message {
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String message;
    private UUID authorId;
    private UUID channelId;
    //필드 변수 : 실행 권한이 없음, 객체의 필드 생성 양식.

    public Message(String message, UUID authorId,  UUID channelId) {
        //생성자: new Message()로 객체 생성시 실행 권한이 생김, 필드 변수 자리에 값을 할당만 함
        this.id = UUID.randomUUID();//쓴 글에 고유 id
        this.createdAt = System.currentTimeMillis();//글쓴 시간
        this.updatedAt = this.createdAt;//최초 업데이트 시간
        this.message = message;//메세지 내용이
        this.authorId = authorId;//글쓴 사람
        this.channelId = channelId;//작성한 채널
    }

        //호출 1: 메인 클레스에서 MessageService.read(message.getId())에서 호출
       //호출 2: JCFMessageService.read() 내부 for문에서 id 비교할 때
      //메세지의 id 값을 반환 한다.
      //반환 후 호출 된 곳으로 복귀
    public UUID getId() {return id;}

    //main에서 message.getCeratedAt()으로 호출
    //필드 변수에 해당 값을 반환 후 메서드 종료. 호출 위치로 복귀
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}
    public String getMessage() {return message;}
    public UUID getAuthorId() {return authorId;}
    public UUID getChannelId() {return channelId;}


    public void updateMessage(String newMessage) {
        //호출: JCFMessageService의 update메서드 안에 foundMessage.updateMessage(message);로 호출
        //필드변수에 새로운 값을 할당하고 메서드 종료 호출 위치로 복귀
        this.message = newMessage;
        this.updatedAt = System.currentTimeMillis();
    }

}
