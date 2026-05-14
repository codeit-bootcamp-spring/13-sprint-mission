package com.sprint.mission.discodeit.app;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.*;

import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.entity.ChannelType.PRIVATE;
import static com.sprint.mission.discodeit.entity.ChannelType.PUBLIC;

public class JavaApplication {

    //구현 테스트
    //등록
    //조회
    //수정
    //수정된 데이터 조회
    //삭제
    //조회를 통해 삭제되었는지 확인
    public static void main(String[] args) {
        // 서비스 생성
        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService();

// User, Channel 생성
        User user1 = userService.createUser("박경석", "pks@naver.com", "1234");
        Channel channel1 = channelService.createChannel("공지채널");

// 생성
        Message message1 = messageService.createContent("안녕하세요!", channel1.getId(), user1.getId());
        Message message2 = messageService.createContent("반갑습니다!", channel1.getId(), user1.getId());
        System.out.println("생성1: " + message1);
        System.out.println("생성2: " + message2);

// 단건 조회
        Message found = messageService.findById(message1.getId());
        System.out.println("단건 조회: " + found);

// 채널별 메시지 조회
        List<Message> messages = messageService.findAllByChannelId(channel1.getId());
        System.out.println("채널 메시지 조회: " + messages);

// 수정
        Message updated = messageService.updateMessage(message1.getId(), "수정된 메시지!");
        System.out.println("수정: " + updated);

// 삭제
        messageService.deleteChannel(message1.getId());
        System.out.println("삭제 확인: " + messageService.findById(message1.getId()));
    }
}
