package com.sprint.mission;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.jcf.*;

import java.util.*;

public class JavaApplication {

    public static void main(String[] args) {


        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService =
                new JCFMessageService(userService, channelService);

        User user1 = new User("신혜선","shinhyesun@yuha.com","abcd1234");
        Channel channel1 = new Channel("JavaSpring","같이 공부해요", ChannelType.PUBLIC);
        Message message1 = new Message("안녕하세요",user1.getId(), channel1.getId());

        User user2 = new User("강동원","dongwontuna1982@yuha.com","KDW810118" );
        Channel channel2 = new Channel( "Python","Python 같이 공부해요", ChannelType.PRIVATE);
        Message message2 = new Message("잘부탁드립니다.",user2.getId(), channel2.getId());

        userService.create(user1);
        userService.create(user2);
        channelService.create(channel1);
        channelService.create(channel2);
        messageService.create(message1);
        messageService.create(message2);

        Message errorMessage = new Message(
                "에러 테스트 메시지",
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        messageService.create(errorMessage);

        System.out.println("=== 단건 조회 ===");
        System.out.println(userService.read(user1.getId()));
        System.out.println(channelService.read(channel1.getId()));
        System.out.println(messageService.read(message1.getId()));


        System.out.println();
        System.out.println(userService.read(user2.getId()));
        System.out.println(channelService.read(channel2.getId()));
        System.out.println(messageService.read(message2.getId()));

        System.out.println();
        System.out.println("=== 다건 조회 ===");

        System.out.println("=== 유저 정보 ===");
        for (User user : userService.readAll()) {
            System.out.println(user);
        }

        System.out.println("=== 채널 정보 ===");
        for (Channel channel : channelService.readAll()) {
            System.out.println(channel);
        }

        System.out.println("=== 메세지 정보 ===");
        for (Message message : messageService.readAll()) {
            System.out.println(message);
        }

        System.out.println();
        System.out.println("=== 수정 ===");
        User findUser = userService.read(user1.getId());
        findUser.setEmail("SYS1234@yuha.com");
        System.out.println(userService.read(user1.getId()));

        System.out.println();
        System.out.println("=== 수정 후 조회 ===");
        System.out.println(userService.read(user1.getId()));

        System.out.println();
        System.out.println("=== 삭제 ===");
        System.out.println("삭제 유저: " + userService.read(user2.getId()));
        System.out.println("삭제 채널: " + channelService.read(channel2.getId()));
        System.out.println("삭제 메시지: " + messageService.read(message2.getId()));

        userService.delete(user2.getId());
        channelService.delete(channel2.getId());
        messageService.delete(message2.getId());

        System.out.println();
        System.out.println("=== 삭제 후 조회 ===");
        System.out.println(userService.readAll());
        System.out.println(channelService.readAll());
        System.out.println(messageService.readAll());

    }

}

